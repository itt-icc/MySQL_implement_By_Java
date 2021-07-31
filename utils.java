import CatalogManager.CatalogManager;
import RecordManager.Condition;
import RecordManager.TableRow;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class utils {
    public static final int NONEXIST = -1;
    public static final String[] OPERATOR = {"<>", "<=", ">=", "=", "<", ">"};

    public static String substring(String str, String start, String end) {
        String regex = start + "(.*)" + end;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) return matcher.group(1);
        else return "";
    }

    public static <T> Vector<T> convert(T[] array) {
        Vector<T> v = new Vector<>();
        for (int i = 0; i < array.length; i++) v.add(array[i]);
        return v;
    }

    //ab <> 'c' | cab ="fabd"  | k=5  | char= '53' | int = 2
    public static Vector<Condition> create_conditon(String[] conSet) throws Exception {
        Vector<Condition> c = new Vector<>();
        for (int i = 0; i < conSet.length; i++) {
            int index = contains(conSet[i], OPERATOR);
            if (index == NONEXIST) throw new Exception("Syntax error: Invalid conditions " + conSet[i]);
            String attr = substring(conSet[i], "", OPERATOR[index]).trim();
            String value = substring(conSet[i], OPERATOR[index], "").trim().replace("\'", "").replace("\"", "");
            c.add(new Condition(attr, OPERATOR[index], value));
        }
        return c;
    }

    public static boolean check_type(String attr, boolean flag) {
        return true;
    }

    public static int contains(String str, String[] reg) {
        for (int i = 0; i < reg.length; i++) {
            if (str.contains(reg[i])) return i;
        }
        return NONEXIST;
    }

    public static void printRow(TableRow row) {
        for (int i = 0; i < row.getSize(); i++) {
            System.out.print(row.getValue(i) + "\t");
        }
        System.out.println();
    }

    public static int get_max_attr_length(Vector<TableRow> tab, int index) {
        int len = 0;
        for (int i = 0; i < tab.size(); i++) {
            int v = tab.get(i).getValue(index).length();
            len = v > len ? v : len;
        }
        return len;
    }

    public static void print_rows(Vector<TableRow> tab, String tabName) {
        if (tab.size() == 0) {
            System.out.println("-->Query ok! 0 rows are selected");
            return;
        }
        int attrSize = tab.get(0).getSize();
        int cnt = 0;
        Vector<Integer> v = new Vector<>(attrSize);
        for (int j = 0; j < attrSize; j++) {
            int len = get_max_attr_length(tab, j);
            String attrName = CatalogManager.getAttriName(tabName, j);
            if (attrName.length() > len) len = attrName.length();
            v.add(len);
            String format = "|%-" + len + "s";
            System.out.printf(format, attrName);
            cnt = cnt + len + 1;
        }
        cnt++;
        System.out.println("|");
        for (int i = 0; i < cnt; i++) System.out.print("-");
        System.out.println();
        for (int i = 0; i < tab.size(); i++) {
            TableRow row = tab.get(i);
            for (int j = 0; j < attrSize; j++) {
                String format = "|%-" + v.get(j) + "s";
                System.out.printf(format, row.getValue(j));
            }
            System.out.println("|");
        }
        System.out.println("-->Query ok! " + tab.size() + " rows are selected");
    }
}