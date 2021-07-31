package IndexManager;


import BufferManager.Block;
import BufferManager.BufferManager;
import CatalogManager.Address;
import CatalogManager.CatalogManager;
import CatalogManager.FieldType;
import CatalogManager.NumType;
import RecordManager.Condition;
import RecordManager.TableRow;


import java.io.*;
import java.util.LinkedHashMap;
import java.util.Vector;

public class IndexManager {

    public IndexManager() {
        //System.out.println("进入索引管理器");
    }

    public static Vector<Address> select(Index idx, Condition cond) throws IllegalArgumentException {
        /*
         *@NOTE  :return the vector that satisfy the condition
         *@param :the index about file information and the condition
         *@return:vector
         */
        //TODO
        String tableName = idx.tableName;
        String attributeName = idx.attributeName;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);
        NumType type = NumType.valueOf(CatalogManager.get_Type(tableName, index));

        //创建树的索引,字符串，小数和整数都实现了compareTO

        switch (type) {
            case INT:
                MyBPlusTree<Integer, Address> intTree = MyINTTreeMapData.get(idx.indexName);
                return IndexManager.<Integer>satisfies_cond(intTree, cond.getOperator(), Integer.parseInt(cond.getValue()));
            case FLOAT:
                MyBPlusTree<Float, Address> floatTree = MyFLOATTreeMapData.get(idx.indexName);
                return IndexManager.<Float>satisfies_cond(floatTree, cond.getOperator(), Float.parseFloat(cond.getValue()));
            case CHAR:
                MyBPlusTree<String, Address> charTree = MyCHARTreeMapData.get(idx.indexName);
                return IndexManager.<String>satisfies_cond(charTree, cond.getOperator(), cond.getValue());
        }
        return null;
    }

    private static int get_store_length(String tableName) {
        /*
         *@NOTE  :get the length for one tuple to store in given table
         *@param :
         *@return:
         */
        //TODO
        int rowLen = CatalogManager.getLength(tableName);
        if (rowLen > FieldType.intSize) {
            return rowLen + FieldType.charSize;
        } else {
            return FieldType.intSize + FieldType.charSize;
        }
    }

    private static <K extends Comparable<? super K>> Vector<Address> satisfies_cond(MyBPlusTree<K, Address> tree, String operator, K key) throws IllegalArgumentException {
        /*
         *@NOTE  :returns a vector of addresses which satisfy the condition
         *@param :
         *@return:
         */
        //TODO
        if (operator.equals("=")) {
            return tree.find_eq(key);
        } else if (operator.equals("<>")) {
            return tree.find_neq(key);
        } else if (operator.equals(">")) {
            return tree.find_greater(key);
        } else if (operator.equals("<")) {
            return tree.find_less(key);
        } else if (operator.equals(">=")) {
            return tree.find_geq(key);
        } else if (operator.equals("<=")) {
            return tree.find_leq(key);
        } else { //undefined operator
            throw new IllegalArgumentException();
        }
    }


    private static TableRow get_tuple(String tableName, Block block, int offset) {
        /*
         *@NOTE  :get the tuple from given table according to stored block and start byte offset
         *@param :String tableName, Block block, int offset
         *@return:TableRow
         */
        //TODO
        int attributeNum = CatalogManager.getAttrNum(tableName);
        String attributeValue = null;
        TableRow result = new TableRow();

        /*跳过合法检验值*/
        offset++;

        for (int i = 0; i < attributeNum/*for each attribute*/; i++) {
            int length = CatalogManager.get_Length(tableName, i);
            String type = CatalogManager.get_Type(tableName, i);
            if (type.equals("CHAR")) {
                int first;
                attributeValue = block.stringRead(offset, length);
                first = /*先找到0的位置*/attributeValue.indexOf(0);
                first = first == -1 ? attributeValue.length() : first;
                /*过滤掉 '\0'*/
                attributeValue = attributeValue.substring(0, first);
            } else if (type.equals("INT")) {
                attributeValue = String.valueOf(block.intRead(offset));
            } else if (type.equals("FLOAT")) {
                attributeValue = String.valueOf(block.floatRead(offset));
            }
            offset += length;
            result.addValue(attributeValue);
        }
        return result;
    }

    /*创建MyINTTreeMapData*/
    private static LinkedHashMap<String, MyBPlusTree<Integer, Address>> MyINTTreeMapData = new LinkedHashMap<>();
    /*创建MyCHARTreeMapData*/
    private static LinkedHashMap<String, MyBPlusTree<String, Address>> MyCHARTreeMapData = new LinkedHashMap<>();
    /*创建MyFLOATTreeMapData*/
    private static LinkedHashMap<String, MyBPlusTree<Float, Address>> MyFLOATTreeMapData = new LinkedHashMap<>();

    private static void build_index(Index idx) throws  RuntimeException {
        /*
         *@NOTE  :Creat index file according the table and its primary key value
         *@param :index information
         *@return:none
         */
        //TODO
        String tableName = idx.tableName;
        String attributeName = idx.attributeName;
        String indexName=idx.indexName;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);
        NumType type = NumType.valueOf(CatalogManager.get_Type(tableName, index));
        switch (type) {
            /*build int index file*/
            case INT: buildIntIndexFile(tableName,attributeName,indexName);break;
            /*build char index file*/
            case CHAR: buildCharIndexFile(tableName,attributeName,indexName);break;
            /*build float index file*/
            case FLOAT: buildFloatIndexFile(tableName,attributeName,indexName);break;
        }
    }

    private static void buildIntIndexFile(String tableName, String attributeName, String indexName) throws  RuntimeException{
        /*
         *@NOTE  :build int index file
         *@param :String tableName, String attributeName, String indexName
         *@return:none
         */
        //TODO
        int tupleNum = CatalogManager.getRowNum(tableName);
        int storeLen = IndexManager.get_store_length(tableName);
        int byteOffset = FieldType.intSize;
        int blockOffset = 0;
        int processNum = 0;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);

        Block block = BufferManager.readBlock(tableName, 0);
        MyBPlusTree<Integer, Address> intTree = new MyBPlusTree<Integer, Address>(4);

        while (processNum < tupleNum) {
            if (byteOffset + storeLen >= Block.BlockSize) { //find next block
                blockOffset++;
                byteOffset = 0;
                block = BufferManager.readBlock(tableName, blockOffset); //read next block
                if (block == null) { //can't get from buffer
                    throw new RuntimeException();
                }
            }
            if (block.intRead(byteOffset) < 0) { //tuple is valid
                Address value = new Address(tableName, blockOffset, byteOffset);//键值存放的是地址
                TableRow row = IndexManager.get_tuple(tableName, block, byteOffset);
                Integer key = Integer.parseInt(row.getValue(index));
                intTree.insert(key, value);
                processNum++; //update processed tuple number
            }
            if(processNum==193)
                System.out.println("begin!!");
            byteOffset += storeLen; //update byte offset
        }
        MyINTTreeMapData.put(indexName, intTree);
    }

    private static void buildCharIndexFile(String tableName,String attributeName,String indexName) throws  RuntimeException{
        /*
         *@NOTE  :build Char index file
         *@param :String tableName, String attributeName, String indexName
         *@return:none
         */
        //TODO
        int tupleNum = CatalogManager.getRowNum(tableName);
        int storeLen = IndexManager.get_store_length(tableName);
        int byteOffset = FieldType.intSize;
        int blockOffset = 0;
        int processNum = 0;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);

        Block block = BufferManager.readBlock(tableName, 0);
        MyBPlusTree<String, Address> charTree = new MyBPlusTree<String,Address>(4);
        while (processNum < tupleNum) {
            if (byteOffset + storeLen >= Block.BlockSize) { //find next block
                blockOffset++;
                byteOffset = 0; //reset byte offset
                block = BufferManager.readBlock(tableName, blockOffset); //read next block
                if (block == null) { //can't get from buffer
                    throw new RuntimeException();
                }
            }
            if (block.intRead(byteOffset) < 0) { //tuple is valid
                Address value = new Address(tableName, blockOffset, byteOffset);
                TableRow row = IndexManager.get_tuple(tableName, block, byteOffset);
                String key = row.getValue(index);
                charTree.insert(key, value);
                processNum++; //update processed tuple number
            }
            byteOffset += storeLen; //update byte offset
        }
        MyCHARTreeMapData.put(indexName, charTree);
    }

    private static void buildFloatIndexFile(String tableName,String attributeName,String indexName) throws  RuntimeException{
        /*
         *@NOTE  :build Float index file
         *@param :String tableName, String attributeName, String indexName
         *@return:none
         */
        //TODO
        int tupleNum = CatalogManager.getRowNum(tableName);
        int storeLen = IndexManager.get_store_length(tableName);
        int byteOffset = FieldType.intSize;
        int blockOffset = 0;
        int processNum = 0;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);

        Block block = BufferManager.readBlock(tableName, 0);
        MyBPlusTree<Float, Address> floatTree = new MyBPlusTree<Float, Address>(4);
        while (processNum < tupleNum) {
            if (byteOffset + storeLen >= Block.BlockSize) { //find next block
                blockOffset++;
                byteOffset = 0; //reset byte offset
                block = BufferManager.readBlock(tableName, blockOffset); //read next block
                if (block == null) { //can't get from buffer
                    throw new RuntimeException();
                }
            }
            if (block.intRead(byteOffset) < 0) { //tuple is valid
                Address value = new Address(tableName, blockOffset, byteOffset);
                TableRow row = IndexManager.get_tuple(tableName, block, byteOffset);
                Float key = Float.parseFloat(row.getValue(index));
                floatTree.insert(key, value);
                processNum++; //update processed tuple number
            }
            byteOffset += storeLen; //update byte offset
        }
        MyFLOATTreeMapData.put(indexName, floatTree);
    }







    public static void delete(Index idx, String key) throws IllegalArgumentException {
        /*
         *@NOTE  :delete the vector that satisfy the condition
         *@param :the index about file information and the condition
         *@return:none
         */
        //TODO
        String tableName = idx.tableName;
        String attributeName = idx.attributeName;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);
        NumType type = NumType.valueOf(CatalogManager.get_Type(tableName, index));
        switch(type) {
            case INT:
                MyBPlusTree<Integer, Address> intTree;
                intTree = MyINTTreeMapData.get(idx.indexName);
                intTree.delete(Integer.parseInt(key));
                break;
            case FLOAT:

                MyBPlusTree<Float, Address> floatTree;
                floatTree = MyFLOATTreeMapData.get(idx.indexName);
                floatTree.delete(Float.parseFloat(key));
                break;
            case CHAR:
                MyBPlusTree<String, Address> charTree;
                charTree = MyCHARTreeMapData.get(idx.indexName);
                charTree.delete(key);
                break;
        }
    }

    public static void insert(Index idx, String key, Address value) throws IllegalArgumentException {
        /*
         *@NOTE  :insert the vector that satisfy the condition
         *@param :the index about file information and the key:value
         *@return:none
         */
        //TODO
        String tableName = idx.tableName;
        String attributeName = idx.attributeName;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);
        NumType type = NumType.valueOf(CatalogManager.get_Type(tableName, index));
        switch(type) {
            case INT:
                MyBPlusTree<Integer, Address> intTree;
                intTree = MyINTTreeMapData.get(idx.indexName);
                intTree.insert(Integer.parseInt(key), value);
                break;
            case FLOAT:
                MyBPlusTree<Float, Address> floatTree;
                floatTree = MyFLOATTreeMapData.get(idx.indexName);
                floatTree.insert(Float.parseFloat(key), value);
                break;
            case CHAR:
                MyBPlusTree<String, Address> charTree;
                charTree = MyCHARTreeMapData.get(idx.indexName);
                charTree.insert(key, value);
                break;
        }
    }

    public static void update(Index idx, String key, Address value) throws IllegalArgumentException {
        /*
         *@NOTE  :update the vector that satisfy the condition
         *@param :the index about file information and the key:value
         *@return:none
         */
        //TODO
        String tableName = idx.tableName;
        String attributeName = idx.attributeName;
        int index = CatalogManager.getAttriIndex(tableName, attributeName);
        NumType type = NumType.valueOf(CatalogManager.get_Type(tableName, index));

        switch(type) {
            case INT:
                MyBPlusTree<Integer, Address> intTree = MyINTTreeMapData.get(idx.indexName);
                intTree.update(Integer.parseInt(key), value);
                break;
            case FLOAT:
                MyBPlusTree<Float, Address> floatTree = MyFLOATTreeMapData.get(idx.indexName);
                floatTree.update(Float.parseFloat(key), value);
                break;
            case CHAR:
                MyBPlusTree<String, Address> charTree = MyCHARTreeMapData.get(idx.indexName);
                charTree.update(key, value);
                break;
        }
    }

    public static void initial_index() throws IOException {
        /*
         *@NOTE  :initial_index
         *@param :none
         *@return:none
         */
        //TODO
        String fileName = "index_catalog";
        File file = new File(fileName);
        if (!file.exists()) return;
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        String tmpIndexName, tmpTableName, tmpAttributeName;
        int tmpBlockNum, tmpRootNum;
        while (dis.available() > 0) {
            tmpIndexName = dis.readUTF();
            tmpTableName = dis.readUTF();
            tmpAttributeName = dis.readUTF();
            tmpBlockNum = dis.readInt();
            tmpRootNum = dis.readInt();
            create_index(new Index(tmpIndexName, tmpTableName, tmpAttributeName, tmpBlockNum, tmpRootNum));
        }
        dis.close();
    }

    public static boolean create_index(Index idx) throws IOException, RuntimeException {
        /*
         *@NOTE  :creat a index that using the index information
         *@param :index information
         *@return:boolean
         */
        //TODO
        String fileName = idx.indexName + ".index";
        build_index(idx);
        //把idx的信息写入到硬盘中
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fos);
        dos.writeUTF(idx.indexName);
        dos.writeUTF(idx.tableName);
        dos.writeUTF(idx.attributeName);
        dos.writeInt(idx.blockNum);
        dos.writeInt(idx.rootNum);
        dos.close();
        return true; //文件读写失败返回false
    }

    public static boolean drop_index(Index idx) {
        /*
         *@NOTE  :delete a index
         *@param :
         *@return:
         */
        //TODO
        String filename = idx.indexName + ".index";
        File file = new File(filename);
        if (file.exists()) file.delete();
        int index = CatalogManager.getAttriIndex(idx.tableName, idx.attributeName);
        NumType type = NumType.valueOf(CatalogManager.get_Type(idx.tableName, index));
        switch (type) {
            case INT: MyINTTreeMapData.remove(idx.indexName);break;
            case CHAR: MyCHARTreeMapData.remove(idx.indexName);break;
            case FLOAT: MyFLOATTreeMapData.remove(idx.indexName);break;
        }
        return true;
    }
}
