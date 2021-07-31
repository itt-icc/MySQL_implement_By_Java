package CatalogManager;

import IndexManager.Index;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class CatalogManager {
    private static LinkedHashMap<String, Table> tables = new LinkedHashMap<>(); //all tables
    private static String tableFile = "table_catalog";
    private static LinkedHashMap<String, Index> indexes = new LinkedHashMap<>();
    private static String indexFile = "index_catalog";

    private static void initTable() throws IOException{
        File file = new File(tableFile);
        if (!file.exists()) return;
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        String tmpTableName, tmpPrimaryKey;
        int tmpIndexNum, tmpAttributeNum, tmpRowNum;

        while (dis.available() > 0) {
            Vector<Attribute> tmpAttributeVector = new Vector<Attribute>();
            Vector<Index> tmpIndexVector = new Vector<Index>();
            tmpTableName = dis.readUTF();
            tmpPrimaryKey = dis.readUTF();
            tmpRowNum = dis.readInt();
            tmpIndexNum = dis.readInt();
            for (int i = 0; i < tmpIndexNum; i++) {
                String tmpIndexName, tmpAttributeName;
                tmpIndexName = dis.readUTF();
                tmpAttributeName = dis.readUTF();
                tmpIndexVector.addElement(new Index(tmpIndexName, tmpTableName, tmpAttributeName));
            }
            tmpAttributeNum = dis.readInt();
            for (int i = 0; i < tmpAttributeNum; i++) {
                String tmpAttributeName, tmpType;
                NumType tmpNumType;
                int tmpLength;
                boolean tmpIsUnique;
                tmpAttributeName = dis.readUTF();
                tmpType = dis.readUTF();
                tmpLength = dis.readInt();
                tmpIsUnique = dis.readBoolean();
                tmpNumType = NumType.valueOf(tmpType);//枚举类型中所在的位置
                tmpAttributeVector.addElement(new Attribute(tmpAttributeName, tmpNumType, tmpLength, tmpIsUnique));
            }
            tables.put(tmpTableName, new Table(tmpTableName, tmpPrimaryKey, tmpAttributeVector, tmpIndexVector, tmpRowNum));
        }
        dis.close();
    }

    private static void initIndex() throws IOException{
        File file = new File(indexFile);
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
            indexes.put(tmpIndexName, new Index(tmpIndexName, tmpTableName, tmpAttributeName, tmpBlockNum, tmpRootNum));
        }
        dis.close();
    }

    public static void initCatalog() throws IOException{
        initTable();
        initIndex();
    }

    private static void storeTable() throws IOException{
        File file = new File(tableFile);
        FileOutputStream outFile = new FileOutputStream(file);
        DataOutputStream outData = new DataOutputStream(outFile);
        Table tmpTable;
        Iterator<Map.Entry<String, Table>> iter = tables.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry = iter.next();
            tmpTable = (Table) entry.getValue();
            outData.writeUTF(tmpTable.tableName);
            outData.writeUTF(tmpTable.primaryKey);
            outData.writeInt(tmpTable.rowNum);
            outData.writeInt(tmpTable.indexNum);
            for(int i=0;i<tmpTable.indexNum;i++){
                Index tmpIndex = tmpTable.indexVector.get(i);
                outData.writeUTF(tmpIndex.indexName);
                outData.writeUTF(tmpIndex.attributeName);
            }
            outData.writeInt(tmpTable.attriNum);
            for(int i=0;i<tmpTable.attriNum;i++){
                Attribute tmPAttribute = tmpTable.attribute.get(i);
                outData.writeUTF(tmPAttribute.attriName);
                outData.writeUTF(tmPAttribute.type.attriType().name());
                outData.writeInt(tmPAttribute.type.getLen());
                outData.writeBoolean(tmPAttribute.isUnique);
            }
        }
        outData.close();
    }

    private static void storeIndex() throws IOException{
        File file = new File(indexFile);
        if (file.exists()) file.delete();
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fos);
        Index tmpIndex;
        //Enumeration<Index> en = indexes.elements();
        Iterator<Map.Entry<String, Index>> iter = indexes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            tmpIndex = (Index) entry.getValue();
            //tmpIndex = en.nextElement();
            dos.writeUTF(tmpIndex.indexName);
            dos.writeUTF(tmpIndex.tableName);
            dos.writeUTF(tmpIndex.attributeName);
            dos.writeInt(tmpIndex.blockNum);
            dos.writeInt(tmpIndex.rootNum);
        }
        dos.close();
    }

    public static void storeCatalog() throws IOException{
        storeTable();
        storeIndex();
    }

    public static void showIndex(){
        Index tmpIndex;
        Iterator<Map.Entry<String, Index>> iter = indexes.entrySet().iterator();
        int idx = 5, tab = 5, attr = 9;
        //System.out.println("There are " + indexes.size() + " indexes in the database: ");
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            tmpIndex = (Index) entry.getValue();
            idx = tmpIndex.indexName.length() > idx ? tmpIndex.indexName.length() : idx;
            tab = tmpIndex.tableName.length() > tab ? tmpIndex.tableName.length() : tab;
            attr = tmpIndex.attributeName.length() > attr ? tmpIndex.attributeName.length() : attr;
        }
        String format = "|%-" + idx + "s|%-" + tab + "s|%-" + attr + "s|\n";
        iter = indexes.entrySet().iterator();
        System.out.printf(format, "INDEX", "TABLE", "ATTRIBUTE");
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            tmpIndex = (Index) entry.getValue();
            System.out.printf(format, tmpIndex.indexName, tmpIndex.tableName, tmpIndex.attributeName);
        }
    }

    public static void showTable(){
        Table tmpTable;
        Attribute tmpAttribute;
        Iterator<Map.Entry<String, Table>> iter = tables.entrySet().iterator();
        //System.out.println("There are " + tables.size() + " tables in the database: ");
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            tmpTable = (Table) entry.getValue();
            System.out.println("[TABLE] " + tmpTable.tableName);
            String format = "|%-" + getMaxAttriLen(tmpTable) + "s";
            format += "|%-5s|%-6s|%-6s|\n";
            System.out.printf(format, "ATTRIBUTE", "TYPE", "LENGTH", "UNIQUE");
            for (int i = 0; i < tmpTable.attriNum; i++) {
                tmpAttribute = tmpTable.attribute.get(i);
                System.out.printf(format, tmpAttribute.attriName, tmpAttribute.type.attriType(), tmpAttribute.type.getLen(), tmpAttribute.isUnique);
            }
            if (iter.hasNext()) System.out.println("--------------------------------");
        }
    }

    public static int getMaxAttriLen(Table table){
        int length = 9;
        for(int i=0;i<table.attribute.size();i++){
            int len = table.attribute.get(i).attriName.length();
            length = len>length?len:length;//如果attribute名字长度比9大则记录。否则就是9(用来调节格式）
        }
        return length;
    }
    // whole GET-table function
    public static Table get_table(String tableName) {return tables.get(tableName);}
    public static Index getIndex(String indexName) {return indexes.get(indexName);}
    public static String getPrimaryKey(String tableName) {return get_table(tableName).primaryKey;}
    public static int getLength(String tableName) {return get_table(tableName).rowLen;} //the length of a tuple
    public static int getAttrNum(String tableName) {return get_table(tableName).attriNum;}
    public static int getRowNum(String tableName) {return get_table(tableName).rowNum;}

    //ATTRIBUTE-CHECK FUNCTION
    private static boolean is_attribute_exist(String tableName,String attributeName){
        if(tables.containsKey(tableName)){
            Table table = get_table(tableName);
            for(int i =0; i<table.attribute.size();i++){
                if (table.attribute.get(i).attriName.equals(attributeName)) return true;
            }
            return false;
        }
        System.out.println("This table "+tableName+" doesn't exist");
        return false;
    }
    public static boolean isPK(String tableName,String attributeName){
        if (tables.containsKey(tableName)){//examine whether this table exists
            Table table = get_table(tableName);
            return table.primaryKey.equals(attributeName);
        }else{
            System.out.println("The table "+tableName+" does not exist");
            return false;
        }
    }

    public static boolean isUnique(String tableName, String attributeName){
        if(tables.containsKey(tableName)){
            Table table = get_table(tableName);
            for (int i=0;i<table.attribute.size();i++){
                Attribute tmpattri = table.attribute.get(i);
                if (tmpattri.attriName.equals(attributeName)) return tmpattri.isUnique;
            }
            System.out.println("This attribute "+attributeName+"does not exist");
            return false;
        }
        System.out.println("The table "+tableName+" does not exist");
        return false;
    }

    public static boolean isIndexKey(String tableName, String attributeName) {
        if (tables.containsKey(tableName)) {
            Table tmpTable = get_table(tableName);
            if (is_attribute_exist(tableName, attributeName)) {
                for (int i = 0; i < tmpTable.indexVector.size(); i++) {
                    if (tmpTable.indexVector.get(i).attributeName.equals(attributeName))
                        return true;
                }
            } else {
                System.out.println("The attribute " + attributeName + " doesn't exist");
            }
        } else
            System.out.println("The table " + tableName + " doesn't exist");
        return false;
    }

    //ATTRIBUTE function
    public static String getAttriName(String table_name, int index){
        return tables.get(table_name).attribute.get(index).attriName;
    }
    /**
     * Function:getAttriIndex
     * Description:get the index according to attribute name
     * @param table_name:name of the table
     * @param Name:name of the attribute
     * @return: -1 not exist >=0 the index
     */
    public static int getAttriIndex(String table_name, String Name){
       Table table = get_table(table_name);
       Attribute tmpAttribute;
       for (int i=0; i <table.attribute.size();i++){
           tmpAttribute=table.attribute.get(i);
           if (tmpAttribute.attriName.equals(Name)) return i;
       }
       System.out.println("Attribute "+Name+" does not exist");
       return -1;
    }
    public static int get_Length(String table_name, int i){
        return get_table(table_name).attribute.get(i).type.getLen();
    }
    /**
     * Function:get_Type
     * Description:The type of certain attribute
     * @return: String
     */
    public static String get_Type(String table_name,int i){
        return get_table(table_name).attribute.get(i).type.attriType().name();
    }
    //INDEX Function
    public static String get_index_name(String tableName, String attributeName) {
        if (tables.containsKey(tableName)) {
            Table tmpTable = get_table(tableName);
            if (is_attribute_exist(tableName, attributeName)) {
                for (int i = 0; i < tmpTable.indexVector.size(); i++) {
                    if (tmpTable.indexVector.get(i).attributeName.equals(attributeName))
                        return tmpTable.indexVector.get(i).indexName;
                }
            } else {
                System.out.println("The attribute " + attributeName + " doesn't exist");
            }
        } else
            System.out.println("The table " + tableName + " doesn't exist");
        return null;
    }
    //CHANGE TABLE ATTRIBUTE
    public static void addRowNum(String tableName) {
        if (tables.containsKey(tableName)) {
            tables.get(tableName).rowNum++;
        }
        else System.out.println("Table "+tableName+"does not exist");
    }
    public static void deleteRowNum(String tableName,int num) {
        if (tables.containsKey(tableName)) {
            tables.get(tableName).rowNum-=num;
        }
        else System.out.println("Table "+tableName+"does not exist");
    }
    public static boolean update_index_table(String indexName, Index tmpIndex) {
        indexes.replace(indexName, tmpIndex);
        return true;
    }

    //FUNCTION BETWEEN CATALOGMANAGER AND INTERFACE
    public static boolean createTable(Table table) throws NullPointerException{
        tables.put(table.tableName,table);
        return true;
    }
    //todo: index(bellow
    public static boolean deleteTable(String tablename) throws NullPointerException{
        Table tmpTable = tables.get(tablename);
        for (int i=0; i <tmpTable.indexVector.size();i++){
            indexes.remove(tmpTable.indexVector.get(i).indexName);
        }
        tables.remove(tablename);
        return true;
    }
    public static boolean create_index(Index newIndex) throws NullPointerException{
        Table tmpTable = get_table(newIndex.tableName);
        tmpTable.indexVector.addElement(newIndex);
        tmpTable.indexNum = tmpTable.indexVector.size();
        indexes.put(newIndex.indexName, newIndex);
        return true;
    }

    public static boolean drop_index(String indexName) throws NullPointerException{
        Index tmpIndex = getIndex(indexName);
        Table tmpTable = get_table(tmpIndex.tableName);
        tmpTable.indexVector.remove(tmpIndex);
        tmpTable.indexNum = tmpTable.indexVector.size();
        indexes.remove(indexName);
        return true;
    }


}
