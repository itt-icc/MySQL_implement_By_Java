package CatalogManager;

import IndexManager.Index;

import java.util.Vector;

public class Table {
    public String tableName;
    public String primaryKey;
    public Vector<Attribute> attribute;
    public Vector<Index> indexVector;
    public int attriNum;
    public int indexNum;
    public int rowNum;//number of tuples
    public int rowLen;//length of a certain tuple

    public Table(String name, String pk, Vector<Attribute>attributes){
        this.tableName = name;
        this.primaryKey = pk;
        this.indexVector = new Vector<>();
        this.indexNum = 0;
        this.attribute = attributes;
        this.attriNum = attributes.size();
        this.rowNum = 0;
        for (int i = 0; i<this.attriNum;i++){
            if(attributes.get(i).attriName.equals(pk)) attributes.get(i).isUnique = true;
            this.rowLen = this.rowLen+attributes.get(i).type.getLen(); //add this attribute's length
        }
    }
    public Table(String tableName, String primaryKey, Vector<Attribute> attributeVector, Vector<Index> indexVector, int rowNum){
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.attribute = attributeVector;
        this.indexVector = indexVector;
        this.indexNum = indexVector.size();
        this.attriNum = attributeVector.size();
        this.rowNum = rowNum;
        for(int i=0;i<attribute.size();i++) this.rowLen+=attribute.get(i).type.getLen();
    }
}
