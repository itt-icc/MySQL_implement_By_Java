package CatalogManager;

public class Attribute {
    public String attriName;
    public FieldType type;
    public boolean isUnique;

    public Attribute(String name, NumType numType, int length, boolean unique){
        this.attriName = name;
        this.type = new FieldType(numType,length);
        this.isUnique = unique;
    }
    public Attribute(String name, NumType numType, boolean unique){
        this.attriName = name;
        this.type = new FieldType(numType);
        this.isUnique = unique;
    }
}
