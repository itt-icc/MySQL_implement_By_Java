package RecordManager;

import BufferManager.Block;
import BufferManager.BufferManager;
import CatalogManager.Address;
import CatalogManager.CatalogManager;
import CatalogManager.FieldType;
import IndexManager.Index;
import IndexManager.IndexManager;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class RecordManager {
    /**
     * Function: getRowLen()
     * Description: get the size of a tuple
     * @param table_name: assign from which table
     * @return: int
     */
    private static int getRowLen(String table_name){
        int length = CatalogManager.getLength(table_name);
        if (length>FieldType.intSize) return length+FieldType.charSize;
        else return FieldType.intSize+FieldType.charSize; //what charsize for
    }

    /**
     * Function: getBlockOffset()
     * Description: find which block this row belongs to
     * @param table_name: from which table
     * @param rowNo: read which row
     * @return: int
     */
    private static int getBlockOffset(String table_name, int rowNo){
        int length = getRowLen(table_name);
        int numRow1 = (Block.BlockSize-FieldType.intSize)/length;// how much tuples contains in block1
        int numRow = Block.BlockSize/length;//how much left
        if (rowNo<numRow1) return 0;
        else return (rowNo-numRow1)/numRow+1;
    }

    /**
     * Function: getByteOffset()
     * Description: Byte offset in this block
     * @param table_name:from which table
     * @param rowNo:read which row
     * @return:
     */
    private static int getByteOffset(String table_name, int rowNo){
        int length = getRowLen(table_name);
        int blockOffset = getBlockOffset(table_name,rowNo);// belongs to which block?
        int numRow1 = (Block.BlockSize-FieldType.intSize)/length;// how much tuples contains in block1
        int numRow = Block.BlockSize/length;//how much left

        if (blockOffset == 0) return rowNo*length+FieldType.intSize;
        else return (rowNo-numRow1-(blockOffset-1)*numRow)*length;
    }

    /**
     * Function: getTupleOffset()
     * Description: find which block this row belongs to
     * @param table_name: from which table
     *
     * @return: int
     */
    private static int getTupleOffset(String table_name,int blockOffset, int byteOffset) {
        int storeLen = getRowLen(table_name);
        int tupleInFirst = (Block.BlockSize - FieldType.intSize) / storeLen; //number of tuples in first block
        int tupleInNext = Block.BlockSize / storeLen; //number of tuples in later block

        if(blockOffset == 0) { //in first block
            return (byteOffset - FieldType.intSize) / storeLen;
        } else { //in later block
            return tupleInFirst + (blockOffset - 1) * tupleInNext + byteOffset / storeLen;
        }
    }

    /**
     * Function:getTuple()
     * Description:put the info into tablerow (all converts 2 string)
     * @param table_name: name of the table
     * @param block: the block it belongs to
     * @param byteOffset: offset of byte
     * @return: TableRow
     */
    private static TableRow getTuple(String table_name, Block block, int byteOffset){
        int attriNum = CatalogManager.getAttrNum(table_name);
        String attriValue = null;
        TableRow result = new TableRow();
        byteOffset = byteOffset+1; //skip valid byte
        for (int i = 0;i<attriNum;i++){
            int length = CatalogManager.get_Length(table_name,i);
            String type = CatalogManager.get_Type(table_name,i);
            if (type.equals("CHAR")){
                int first;
                attriValue = block.stringRead(byteOffset,length);
                first = attriValue.indexOf(0);
                first = first ==-1 ? attriValue.length():first;
                attriValue = attriValue.substring(0,first); //filter '\0'
            }
            else if(type.equals("INT")){
                attriValue = String.valueOf(block.intRead(byteOffset));//int->string
            }else if(type.equals("FLOAT")){
                attriValue = String.valueOf(block.floatRead(byteOffset));//float->string
            }
            byteOffset = byteOffset+length;
            result.addValue(attriValue);
        }
        return result;
    }

    /**
     * Function:writeTuple()
     * Description:write info into table-->insert
     * @param table_name: name of the table
     * @param data: data wants to be stored
     * @param block: the block it belongs to
     * @param offset: offset of byte
     * @return: void
     */
    private static void writeTuple(String table_name, TableRow data, Block block,int offset){
        int attriNum = CatalogManager.getAttrNum(table_name);
        block.intWrite(offset,-1); //this tuple is valid
        offset ++;

        for (int i = 0; i<attriNum; i++){
            int length = CatalogManager.get_Length(table_name,i); //length of attribute
            String type = CatalogManager.get_Type(table_name,i); //type of attribute
            if (type.equals("CHAR")){
                byte[] info = new byte[length];
                Arrays.fill(info,(byte)0); //fill the size of the char to 4times
                block.dataWrite(offset,info);
                block.stringWrite(offset,data.getValue(i));
            }else if (type.equals("INT")){
                block.intWrite(offset,Integer.parseInt(data.getValue(i))); //string->int
            }else if (type.equals("FLOAT")){
                block.floatWrite(offset,Float.parseFloat(data.getValue(i))); //string->float
            }
            offset +=length;
        }
    }

    /**
     * Function:checkType()
     * Description:check whether same type+same length
     * @param type: type of attribute
     * @param length: length of attribute
     * @param value: data
     * @return: boolean
     */
    private static boolean checkType(String type, int length, String value) throws Exception{
        switch(type){
            case "INT":
                try{
                    Integer.parseInt(value);
                }catch (NumberFormatException e){
                    throw new IllegalArgumentException(value + "doesn't match INT type or Overflow");
                }
                break;
            case "FLOAT":
                try{
                    Float.parseFloat(value);
                }catch (NumberFormatException e){
                    throw new IllegalArgumentException(value + "doesn't match FLOAT type or Overflow");
                }
                break;
            case "CHAR":
                if (length<value.length()) throw new IllegalArgumentException("The length of char "+value+" doesn't match");
                //only <; > fill with 0
                break;
            default: throw new IllegalArgumentException("Undefined Type, ONLY Support: INT|FLOAT|CHAR");
        }
        return true;
    }

    /**
     * Function:checkRow()
     * Description:check whether the tuple satisfy attribute def--> same type+same length+num of attribute
     * @param table_name: name of the table
     * @param data: data wants to be store
     * @return: boolean
     */
    private static boolean checkRow(String table_name,TableRow data) throws Exception{
        if (CatalogManager.getAttrNum(table_name) != data.getSize())
            throw new IllegalArgumentException("The number of Attribute doesn't match");
        for (int i=0; i < data.getSize();i++){
            String type = CatalogManager.get_Type(table_name,i);
            int length = CatalogManager.get_Length(table_name,i);
            if (!checkType(type,length,data.getValue(i))) return false;
        }
        return true;
    }

    /**
     * Function:checkCondition()
     * Description:check whether the tuple satisfy attribute def--> same type+same length+num of attribute
     * @param table_name: name of the table
     * @param conditions: conditions we want to check
     * @return: boolean
     */
    private static boolean checkCondition(String table_name,Vector<Condition>conditions) throws Exception{
        for (int i = 0; i<conditions.size();i++){
            String attriName = conditions.get(i).getName();
            int index = CatalogManager.getAttriIndex(table_name,attriName);
            if(index == -1)
                throw new IllegalArgumentException("Attribute "+attriName+" DOESN'T EXIST");// no such attribute
            String type = CatalogManager.get_Type(table_name,index);
            int length = CatalogManager.get_Length(table_name,index);
            if (!checkType(type,length,conditions.get(i).getValue()))
                return false; //mainly for char
        }
        return true;
    }

    /**
     * Function: createTable()
     * Description: create a table
     * @param table_name: the name of the table
     * @return: boolean
     */
    public static boolean createTable(String table_name) throws Exception{
        File file = new File(table_name);
        if (file.exists()) throw new NullPointerException(); //todo: throw message table exsits
        else file.createNewFile(); //not exists then create a certain file for it
        Block block = BufferManager.readBlock(table_name,0);
        if (block == null) {
            throw new NullPointerException();}//read fail//no file//no buffer}
            else{
                block.intWrite(0, -1);
                return true;
            }//-1 means the available space is at the end of all tuples
    }
    /**
     * Function: deleteTable()
     * Description: delete the whole table
     * @param table_name: the name of table
     * @return: boolean
     */
    public static boolean deleteTable(String table_name) throws Exception{
        File file = new File(table_name);
        if (file.delete()){
            //delete successful
            BufferManager.setInvalid(table_name);//set all block invalid or reset this block
            return true;
        }
        else throw new NullPointerException();
    }

    /**
     * Function: insert()
     * Description: insert the data into the buffer
     * @param table_name: name of the table
     * @param data: data wanted to be inserted
     * @return: Address
     */
    public static Address insert(String table_name, TableRow data) throws Exception{
        int tupleNum = CatalogManager.getRowNum(table_name);// how many tuples it includes
        Block firstBlock = BufferManager.readBlock(table_name,0);
        if (firstBlock == null) throw new NullPointerException();
        //System.out.println(data);
        if(!checkRow(table_name, data)) return null;// data illegal
        firstBlock.setLock(true);
        int freeTuple = firstBlock.intRead(0);// first store the position of free address
        int tupleOffset;
        if(freeTuple<0) tupleOffset=tupleNum; //tail of the file
        else tupleOffset=freeTuple;

        int blockOffset = getBlockOffset(table_name,tupleOffset);
        int byteOffset = getByteOffset(table_name,tupleOffset);
        Block insertBlock = BufferManager.readBlock(table_name,blockOffset);
        if (insertBlock == null){
            firstBlock.setLock(false); //unlock
            return null;
        }
        if(freeTuple>=0){
            freeTuple = insertBlock.intRead(byteOffset+1); //todo:why? nextTuple as an attribute
            firstBlock.intWrite(0,freeTuple);
        }
        firstBlock.setLock(false);//unlock
        writeTuple(table_name,data,insertBlock,byteOffset);
        return new Address(table_name,blockOffset,byteOffset);
    }

    /**
     * Function: delete()
     * Description: insert the data into the buffer
     * @param table_name: name of the table
     * @param conditions: according to which conditions
     * @return: int(the amount of deleted tuples)
     */
    public static int delete(String table_name,Vector<Condition> conditions) throws Exception{
        int tupleNum = CatalogManager.getRowNum(table_name);
        int storeSize = getRowLen(table_name);

        int processNum = 0;
        int byteOffset = FieldType.intSize;
        int blockOffset = 0;
        int deleteNum = 0;

        Block firstBlock = BufferManager.readBlock(table_name,0);
        Block laterBlock = firstBlock;
        if(firstBlock == null) throw new NullPointerException();
        if(!checkCondition(table_name,conditions)) return 0;
        firstBlock.setLock(true);
        for (int currentNum = 0;processNum<tupleNum;currentNum++){
            if (byteOffset+storeSize>=Block.BlockSize){
                blockOffset ++;
                byteOffset = 0;
                laterBlock = BufferManager.readBlock(table_name,blockOffset);
                if(laterBlock == null){
                    firstBlock.setLock(false);
                    return deleteNum;
                }
            }
            if(laterBlock.intRead(byteOffset)<0){
                //valid tuple
                int i=0;
                TableRow newRow = getTuple(table_name,laterBlock,byteOffset);
                for(i=0;i<conditions.size();i++){
                    if(!conditions.get(i).satisfy(table_name,newRow)) break;//逐条检查条件
                }
                if(i==conditions.size()){//satisfy all conditions
                    laterBlock.intWrite(byteOffset,0);//not valid
                    laterBlock.intWrite(byteOffset+1,firstBlock.intRead(0));
                    firstBlock.intWrite(0,currentNum);
                    deleteNum++;
                    //TODO:Index 相关删除
                    for(int j = 0;j < newRow.getSize();j++) { //delete index---》需要后面继续看
                        String attrName = CatalogManager.getAttriName(table_name, j);
                        if (CatalogManager.isIndexKey(table_name, attrName)) {
                            String indexName = CatalogManager.get_index_name(table_name, attrName);
                            Index index = CatalogManager.getIndex(indexName);
                            IndexManager.delete(index, newRow.getValue(j));
                        }
                    }
                }
                processNum++;
            }
            byteOffset=byteOffset+storeSize;
        }
        firstBlock.setLock(false);
        return deleteNum;
    }

    public static int delete(Vector<Address> address, Vector<Condition> conditions) throws Exception {
        if(address.size() == 0) //empty address
            return 0;

        Collections.sort(address); //sort address
        String tableName = address.get(0).getFilename(); //get table name

        int blockOffset = 0,blockOffsetPre = -1; //current and previous block offset
        int byteOffset = 0; //current byte offset
        int tupleOffset = 0; //tuple offset in file

        Block headBlock = BufferManager.readBlock(tableName, 0); //get head block
        Block deleteBlock = null;

        if(headBlock == null)  //can't get from buffer
            throw new NullPointerException();
        if(!checkCondition(tableName, conditions))  //check condition
            return 0;

        headBlock.setLock(true); //lock head block for free list update

        int deleteNum = 0; // number of delete tuple
        for(int i = 0;i < address.size();i++) { //for each address
            blockOffset = address.get(i).getBlockOffset(); //read block and byte offset
            byteOffset = address.get(i).getByteOffset();
            tupleOffset = getTupleOffset(tableName, blockOffset, byteOffset);

            if(i == 0 || blockOffset != blockOffsetPre) { //not in same block
                deleteBlock = BufferManager.readBlock(tableName, blockOffset); // read a new block
                if(deleteBlock == null) { //can't get from buffer
                    headBlock.setLock(false);
                    return deleteNum;
                }
            }

            if (deleteBlock.intRead(byteOffset) < 0) { //tuple is valid
                int j;
                TableRow newRow = getTuple(tableName, deleteBlock, byteOffset);
                for(j = 0;j < conditions.size();j++) { //check all conditions
                    if(!conditions.get(j).satisfy(tableName, newRow))
                        break;
                }
                if(j == conditions.size()) { //all satisfy
                    deleteBlock.intWrite(byteOffset, 0); //set valid byte to 0
                    deleteBlock.intWrite(byteOffset + 1, headBlock.intRead(0)); //set free address
                    headBlock.intWrite(0, tupleOffset); //write delete offset to head
                    deleteNum++;
                    for(int k = 0;k < newRow.getSize();k++) { //delete index
                        String attrName = CatalogManager.getAttriName(tableName, k);
                        if (CatalogManager.isIndexKey(tableName, attrName)) {
                            String indexName = CatalogManager.get_index_name(tableName, attrName);
                            Index index = CatalogManager.getIndex(indexName);
                            IndexManager.delete(index, newRow.getValue(k));//update
                        }
                    }
                }
            }
            blockOffsetPre = blockOffset;
        }

        headBlock.setLock(false); //unlock head block
        return deleteNum;
    }


    /**
     * Function: select()
     * Description: to implement select function of database
     * @param table_name: the name of table
     * @param condition: the conditions we hope to satisfy
     * @return: Vector<TableRow>
     */
    public static Vector<TableRow> select(String table_name,Vector<Condition> condition) throws Exception{
        int rowNum = CatalogManager.getRowNum(table_name); // how many row this table includes
        int rowLen = getRowLen(table_name); // get the length of a row

        int count = 0;
        int blockOffset = 0;
        int byteOffset = FieldType.intSize;//skip head this integer is used to show the position of valid space
        Vector<TableRow> result = new Vector<>();
        // check each tuple in the table
        Block block = BufferManager.readBlock(table_name,0);// the first
        if (block == null) //no buffer||no table||no file(read error)
            throw new NullPointerException();
        if(!checkCondition(table_name,condition)) return result; //condition not satisfy return 0

        while (count<rowNum){
            if (byteOffset + rowLen >= Block.BlockSize){
                //if overflow, we need to get next block
                blockOffset=blockOffset+1;
                byteOffset = 0;
                block = BufferManager.readBlock(table_name,blockOffset); //read next
                if (block == null) return result;
            }

            if (block.intRead(byteOffset)<0) { //valid, not deleted
                int i=0;
                TableRow newRow = getTuple(table_name,block,byteOffset);
                for(i = 0; i<condition.size();i++){
                    if(!condition.get(i).satisfy(table_name,newRow)) break;;
                }
                if(i==condition.size())result.add(newRow);
                count = count+1;
            }
            byteOffset = byteOffset+rowLen;
        }
        return result;
    }

    public static Vector<TableRow> select(Vector<Address> address, Vector<Condition> conditions) throws Exception{
        if(address.size() == 0) //empty address
            return new Vector<>();
        Collections.sort(address); //sort address
        String tableName = address.get(0).getFilename(); //get table name
        int blockOffset = 0, blockOffsetPre = -1; //current and previous block offset
        int byteOffset = 0; //current byte offset

        Block block = null;
        Vector<TableRow> result = new Vector<>();

        if(!checkCondition(tableName, conditions))  //check condition
            return result;

        for(int i = 0;i < address.size(); i++) { //for each later address
            blockOffset = address.get(i).getBlockOffset(); //read block and byte offset
            byteOffset = address.get(i).getByteOffset();
            if (i == 0 || blockOffset != blockOffsetPre) { //not in same block as previous
                block = BufferManager.readBlock(tableName, blockOffset); // read a new block
                if(block == null) {
                    if (i == 0)
                        throw new NullPointerException();
                }
            }
            if (block.intRead(byteOffset) < 0) { //tuple is valid
                int j;
                TableRow newRow = getTuple(tableName, block, byteOffset);
                for(j = 0;j < conditions.size();j++) { //check all conditions
                    if(!conditions.get(j).satisfy(tableName,newRow))
                        break;
                }
                if(j == conditions.size()) { //all satisfy
                    result.add(newRow); //add tuple
                }
            }
            blockOffsetPre = blockOffset;
        }
        return result;
    }
    public static Vector<TableRow> project(String table_name,Vector<TableRow> rows, Vector<String> attributs)throws Exception{
        int attriNum = CatalogManager.getAttrNum(table_name);
        Vector<TableRow> result = new Vector<>();
        for(int i=0;i<rows.size();i++){
            TableRow newRow = new TableRow();
            for(int j=0;j<attributs.size();j++){
                int index = CatalogManager.getAttriIndex(table_name,attributs.get(j));
                if(index==-1) throw new IllegalArgumentException("Cannot find attribute "+attributs.get(j));
                else
                    newRow.addValue(rows.get(i).getValue(index));
            }
            result.add(newRow);
        }
        return result;
    }
    public static void storeRecord(){
        BufferManager.deleteBuffer();
    }
}
