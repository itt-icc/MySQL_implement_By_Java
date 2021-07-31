package CatalogManager;

public class FieldType {
    private NumType type; //type of attributes
    private int length;

    public static final int charSize = 1;
    public static final int intSize = 4;
    public static final int floatSize = 4;

    /**
     * Function: FieldType()
     * Description: default construction
     */
    FieldType(){}

    /**
     * Function: FieldType(NumType numtype)
     * Description: construction for int and float
     */
    FieldType(NumType numtype){
        type = numtype;
        length = 1;
    }
    /**
     * Function: FieldType(NumType numtype,int length)
     * Description: construction for char
     */
    FieldType(NumType numtype, int clength){
        type = numtype;
        length = clength;
    }
    /**
     * Function: attriType()
     * Description: get the type of the attribute
     * @return: Numtype
     */
    NumType attriType(){return this.type;}
    /**
     * Function:getLen()
     * Description:get the byte length of A attribute
     * @return: int
     */
    int getLen(){
        switch(this.type){
            case CHAR:
                return this.length*charSize;
            case INT:
                return intSize;
            case FLOAT:
                return floatSize;
            default:
                return 0;
        }
    }
}
