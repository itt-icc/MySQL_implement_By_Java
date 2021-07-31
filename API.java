import BufferManager.BufferManager;
import CatalogManager.Address;
import CatalogManager.CatalogManager;
import CatalogManager.Table;
import IndexManager.Index;
import IndexManager.IndexManager;
import RecordManager.Condition;
import RecordManager.RecordManager;
import RecordManager.TableRow;

import java.io.IOException;
import java.util.Vector;

public class API {

    public static void Init() throws Exception{
        try{
            BufferManager.initBuffer();
            CatalogManager.initCatalog(); //read all tables
            IndexManager.initial_index();
        }catch(Exception e){
            throw new RunTimeError("Failed to Initialize");
        }
    }
    public static void store() throws Exception{
        CatalogManager.storeCatalog(); //store table metadata
        RecordManager.storeRecord(); //store buffer's record data
    }

    public static boolean create_table(String tableName, Table table) throws Exception{
        try{
            if(RecordManager.createTable(tableName)&&CatalogManager.createTable(table)){
                //record创建table 以及相关数据， catalog 存储metadata
                String indexName = tableName+"_index";
                //construction of index
                Index index = new Index(indexName,tableName,CatalogManager.getPrimaryKey(tableName));
                IndexManager.create_index(index);
                CatalogManager.create_index(index);//into metadata
                return true;
            }
        }catch (NullPointerException e){
            throw  new RunTimeError("Table "+tableName+" already exist!");//todo
        }catch (IOException e){
            throw  new  RunTimeError("failed to create an index");
        }
        throw new RunTimeError("Failed to create table");
    }
    public static boolean drop_table(String tabName) throws Exception {
        try {
            for (int i = 0; i < CatalogManager.getAttrNum(tabName); i++) {
                String attrName = CatalogManager.getAttriName(tabName, i);
                String indexName = CatalogManager.get_index_name(tabName, attrName);  //find index if exists
                if (indexName != null) {
                    IndexManager.drop_index(CatalogManager.getIndex(indexName)); //drop index at Index Manager
                }
            }//以上是对于index的处理
            if (CatalogManager.deleteTable(tabName) && RecordManager.deleteTable(tabName)) return true;
        } catch (NullPointerException e) {
            throw new RunTimeError(  "Table " + tabName + " does not exist!");
        }
        throw new RunTimeError(  "Failed to drop table " + tabName);
    }

    public static boolean create_index(Index index) throws Exception {
        if (IndexManager.create_index(index) && CatalogManager.create_index(index)) return true;
        throw new RunTimeError(  "Failed to create index " + index.attributeName + " on table " + index.tableName);
    }

    public static boolean drop_index(String indexName) throws Exception {
        Index index = CatalogManager.getIndex(indexName);
        if (IndexManager.drop_index(index) && CatalogManager.drop_index(indexName)) return true;
        throw new RunTimeError(  "Failed to drop index " + index.attributeName + " on table " + index.tableName);
    }

    public static boolean insert_row(String tabName, TableRow row) throws Exception {
        try {
            //这里可以应该检查一下
            //System.out.println("Into api.insert_row()");
            Address recordAddr = RecordManager.insert(tabName, row);  //insert
            int attrNum = CatalogManager.getAttrNum(tabName);
            //是针对的index的处理
            for (int i = 0; i < attrNum; i++) {
                String attrName = CatalogManager.getAttriName(tabName, i);
                String indexName = CatalogManager.get_index_name(tabName, attrName);  //find index if exists
                if (indexName != null) {  //index exists, then need to insert the key to BPTree
                    Index index = CatalogManager.getIndex(indexName); //get index
                    String key = row.getValue(i);  //get value of the key
                    IndexManager.insert(index, key, recordAddr);  //insert to index manager key为值， recordAddr为拟地址指针
                    CatalogManager.update_index_table(indexName, index); //update index
                }
            }
            CatalogManager.addRowNum(tabName);  //update number of records in catalog
            return true;
        } catch (NullPointerException e){
            throw new RunTimeError( "Table " + tabName + " does not exist!");
        } catch (IllegalArgumentException e) {
            throw new RunTimeError( e.getMessage());
        } catch (Exception e) {
            throw new RunTimeError( "Failed to insert a row on table " + tabName);
        }
    }

    private static Condition findIndexCondition(String tabName, Vector<Condition> conditions) throws Exception {
        //查找condition中的哪些属性是存在索引树的，若有多个属性，则取第一个
        Condition condition = null;
        for (int i = 0; i < conditions.size(); i++) {
            if (CatalogManager.get_index_name(tabName, conditions.get(i).getName()) != null) { //存在与这个condition属性相应的index
                condition = conditions.get(i);
                conditions.remove(condition);
                break;
            }
        }
        return condition;
    }

    public static int delete_row(String tabName, Vector<Condition> conditions) throws Exception {
        Condition condition = API.findIndexCondition(tabName, conditions);
        int numberOfRecords = 0;
        if (condition != null) {
            try {
                String indexName = CatalogManager.get_index_name(tabName, condition.getName());
                Index idx = CatalogManager.getIndex(indexName);//得到对应索引
                Vector<Address> addresses = IndexManager.select(idx, condition);//得到该索引可找到的满足的条件
                if (addresses != null) {
                    numberOfRecords = RecordManager.delete(addresses, conditions); //利用剩下的继续进行检索
                }
            } catch (NullPointerException e) {
                throw new RunTimeError(  "Table " + tabName + " does not exist!");
            } catch (IllegalArgumentException e) {
                throw new RunTimeError(  e.getMessage());
            } catch (Exception e) {
                throw new RunTimeError( "Failed to delete on table " + tabName);
            }
        } else {//如果index的中没有该项索引，则采用record的方法来进行调用
            try {
                numberOfRecords = RecordManager.delete(tabName, conditions);
            }  catch (NullPointerException e) {
                throw new RunTimeError(  "Table " + tabName + " does not exist!");
            } catch (IllegalArgumentException e) {
                throw new RunTimeError( e.getMessage());
            }
        }
        CatalogManager.deleteRowNum(tabName, numberOfRecords);
        return numberOfRecords;
    }

    public static Vector<TableRow> select(String tabName, Vector<String> attriName, Vector<Condition> conditions) throws Exception {
        Vector<TableRow> resultSet = new Vector<>();
        Condition condition = API.findIndexCondition(tabName, conditions);
        if (condition != null) {//若index中存在condition属性
            try {
                String indexName = CatalogManager.get_index_name(tabName, condition.getName());
                Index idx = CatalogManager.getIndex(indexName);
                Vector<Address> addresses = IndexManager.select(idx, condition);
                if (addresses != null) {
                    resultSet = RecordManager.select(addresses, conditions);
                }
            } catch (NullPointerException e) {
                throw new RunTimeError(  "Table " + tabName + " does not exist!");
            } catch (IllegalArgumentException e) {
                throw new RunTimeError( e.getMessage());
            } catch (Exception e) {
                throw new RunTimeError( "Failed to select from table " + tabName);
            }
        } else {
            try {//若不存在
                resultSet = RecordManager.select(tabName, conditions);
            } catch (NullPointerException e) {
                throw new RunTimeError( "Table " + tabName + " does not exist!");
            } catch (IllegalArgumentException e) {
                throw new RunTimeError( e.getMessage());
            }
        }

        if (!attriName.isEmpty()) {//根据所需的attriName进行投影
            try {
                return RecordManager.project(tabName, resultSet, attriName);
            } catch (NullPointerException e) {
                throw new RunTimeError( "Table " + tabName + " does not exist!");
            } catch (IllegalArgumentException e) {
                throw new RunTimeError( e.getMessage());
            }
        } else {
            return resultSet;
        }
    }
}