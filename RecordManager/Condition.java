package RecordManager;
import CatalogManager.CatalogManager;

public class Condition {
    private String name; //attribute name
    private String value; //attribute value
    private String operator; //operator

    public Condition(String name, String operator, String value){
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    public boolean satisfy(String table_name, TableRow data){
        int index = CatalogManager.getAttriIndex(table_name,this.name);
        String type = CatalogManager.get_Type(table_name,index);
        if (type.equals("CHAR")){
            String value1 = data.getValue(index);
            String value2 = this.value;
            if (this.operator.equals("=")) return value1.compareTo(value2)==0;
            else if(this.operator.equals("<>")||this.operator.equals("!=")) return value1.compareTo(value2)!=0;
            else if(this.operator.equals(">")) return value1.compareTo(value2)>0;
            else if(this.operator.equals("<")) return value1.compareTo(value2)<0;
            else if(this.operator.equals(">=")) return value1.compareTo(value2)>=0;
            else if(this.operator.equals("<=")) return value1.compareTo(value2)<=0;
            else return false;//undefined
        }
        else if (type.equals("INT")){
            int value1 = Integer.parseInt(data.getValue(index));
            int value2 = Integer.parseInt(this.value);
            switch (this.operator) {
                case "=":
                    return value1 == value2;
                case "<>":
                case "!=":
                    return value1 != value2;
                case ">":
                    return value1 > value2;
                case "<":
                    return value1 < value2;
                case ">=":
                    return value1 >= value2;
                case "<=":
                    return value1 <= value2;
                default:
                    return false;
            }
        }
        else if (type.equals("FLOAT")){
            float value1 = Float.parseFloat(data.getValue(index));
            float value2 = Float.parseFloat(this.value);
            switch (this.operator) {
                case "=":
                    return value1 == value2;
                case "<>":
                case "!=":
                    return value1 != value2;
                case ">":
                    return value1 > value2;
                case "<":
                    return value1 < value2;
                case ">=":
                    return value1 >= value2;
                case "<=":
                    return value1 <= value2;
                default:
                    return false;
            }
        }else return false;
    }
    public String getName() {return this.name;}
    public String getValue() {return this.value;}
    public String getOperator() {return this.operator;}

}
