package RecordManager;
import java.util.Vector;

public class TableRow {
    private Vector<String> attriValue;
    public TableRow(){
        attriValue=new Vector<>();
    }
    public void addValue(String value){
        this.attriValue.add(value);
    }
    public String getValue(int index){
        return attriValue.get(index);
    }
    public int getSize(){
        return this.attriValue.size();// length of attribute value
    }
}
