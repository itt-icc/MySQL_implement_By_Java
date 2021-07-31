import CatalogManager.Attribute;
import CatalogManager.CatalogManager;
import CatalogManager.NumType;
import CatalogManager.Table;
import IndexManager.Index;
import IndexManager.IndexManager;
import RecordManager.Condition;
import RecordManager.RecordManager;
import RecordManager.TableRow;

import java.io.*;
import java.util.Vector;

public class Interpreter {
    private static int execFile = 0;
    private static void interpret(BufferedReader reader) throws IOException {
        /*
        *@NOTE  :interpret 解释器
        *@param :reader
        *@return:none
        */
        //TODO
        String restState = ""; //rest statement after ';' in last line

        while (true) { //read for each statement
            int index;
            String line;
            StringBuilder statement = new StringBuilder();
            if (restState.contains(";")) { // resetLine contains whole statement
                index = restState.indexOf(";");
                statement.append(restState.substring(0, index));
                restState = restState.substring(index + 1); //""
            } else {
                statement.append(restState); //add rest line
                statement.append(" ");
                if (execFile == 0)
                    System.out.print(">>>");

                while (true) {  //read whole statement until ';'
                    line = reader.readLine();
                    if (line == null) { //read the file tail
                        reader.close();
                        return;
                    } else if (line.contains(";")) { //last line
                        index = line.indexOf(";");
                        statement.append(line.substring(0, index));
                        restState = line.substring(index + 1); //set reset statement
                        break;
                    } else {
                        statement.append(line);
                        statement.append(" ");
                        if (execFile == 0)
                            System.out.print(">>>");
//                        System.out.print(">>>"); //next line
                    }
                }
            }

            //after get the whole statement
            String result = statement.toString().trim().replaceAll("\\s+", " "); //字符串转换--》去除头尾空白符--》
            String[] tokens = result.split(" ");

            try {
                if (tokens.length == 1 && tokens[0].equals(""))
                    throw new SnytaxError( "No statement specified");

                switch (tokens[0]) { //match keyword
                    case "create":
                        if (tokens.length == 1)
                            throw new SnytaxError( "Can't find create object");
                        switch (tokens[1]) {
                            case "table":
                                parse_create_table(result); //create table
                                break;
                            case "index":
                                parse_create_index(result); //create index
                                break;
                            default:
                                throw new SnytaxError(  "Can't identify " + tokens[1]);
                        }
                        break;
                    case "drop":
                        if (tokens.length == 1)
                            throw new SnytaxError( "Can't find drop object");
                        switch (tokens[1]) {
                            case "table":
                                parse_drop_table(result);
                                break;
                            case "index":
                                parse_drop_index(result);
                                break;
                            default:
                                throw new SnytaxError( "Can't identify " + tokens[1]);
                        }
                        break;
                    case "select":
                        parse_select(result);
                        break;
                    case "insert":
                        parse_insert(result);
                        break;
                    case "delete":
                        parse_delete(result);
                        break;
                    case "quit":
                        parse_quit(result, reader);
                        break;
                    case "execfile":
                        parse_sql_file(result);
                        break;
                    case "show":
                        parse_show(result);
                        break;
                    default:
                        throw new SnytaxError( "Can't identify " + tokens[0]);
                }
            }
            catch(RunTimeError e){
                e.printMsg();
            }
            catch (SnytaxError e){
                e.printMsg();
            }
            catch (Exception e) {
                System.out.println("Default error: " + e.getMessage());
            }
        }
    }

    private static void parse_show(String statement) throws Exception {
        String type = utils.substring(statement, "show ", "").trim();// 删除头尾空白符
        if (type.equals("table")) {
            CatalogManager.showTable();
        } else if (type.equals("index")) {
            CatalogManager.showIndex();
        } else throw new SnytaxError(  "Can not find valid key word after 'show'!");
    }

    private static void parse_create_table(String statement) throws Exception {
        statement = statement.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
        statement = statement.replaceAll(" *, *", ",");
        statement = statement.trim();
        statement = statement.replaceAll("^create table", "").trim(); //skip create table keyword

        int startIndex, endIndex;
        if (statement.equals("")) //no statement after create table
            throw new SnytaxError(  "Must specify a table name");

        endIndex = statement.indexOf(" ");
        if (endIndex == -1)  //no statement after create table xxx
            throw new SnytaxError(  "Can't find attribute definition");

        String tableName = statement.substring(0, endIndex); //get table name
        startIndex = endIndex + 1; //start index of '('
        if (!statement.substring(startIndex).matches("^\\(.*\\)$"))  //check brackets
            throw new SnytaxError( "Can't not find the definition brackets in table " + tableName);

        int length;
        String[] attrParas, attrsDefine;
        String attrName, attrType, attrLength = "", primaryName = "";
        boolean attrUnique;
        Attribute attribute;
        Vector<Attribute> attrVec = new Vector<>();

        attrsDefine = statement.substring(startIndex + 1).split(","); //get each attribute definition
        for (int i = 0; i < attrsDefine.length; i++) { //for each attribute
            if (i == attrsDefine.length - 1) { //last line
                attrParas = attrsDefine[i].trim().substring(0, attrsDefine[i].length() - 1).split(" "); //remove last ')'
            } else {
                attrParas = attrsDefine[i].trim().split(" ");
            } //split each attribute in parameters: name, type,（length) (unique)

            if (attrParas[0].equals("")) { //empty
                throw new SnytaxError( "Empty attribute in table " + tableName);
            } else if (attrParas[0].equals("primary")) { //primary key definition
                if (attrParas.length != 3 || !attrParas[1].equals("key"))  //not as primary key xxxx
                    throw new SnytaxError(  "Error definition of primary key in table " + tableName);
                if (!attrParas[2].matches("^\\(.*\\)$"))  //not as primary key (xxxx)
                    throw new SnytaxError(  "Error definition of primary key in table " + tableName);
                if (!primaryName.equals("")) //already set primary key
                    throw new SnytaxError( "Redefinition of primary key in table " + tableName);

                primaryName = attrParas[2].substring(1, attrParas[2].length() - 1); //set primary key
            } else { //ordinary definition
                if (attrParas.length == 1)  //only attribute name
                    throw new SnytaxError( "Incomplete definition in attribute " + attrParas[0]);
                attrName = attrParas[0]; //get attribute name
                attrType = attrParas[1]; //get attribute type
                for (int j = 0; j < attrVec.size(); j++) { //check whether name redefines
                    if (attrName.equals(attrVec.get(j).attriName))
                        throw new SnytaxError( "Redefinition in attribute " + attrParas[0]);
                }
                if (attrType.equals("int") || attrType.equals("float")) { //check type
                    endIndex = 2; //expected end index
                } else if (attrType.equals("char")) {
                    if (attrParas.length == 2)  //no char length
                        throw new SnytaxError(  "ust specify char length in " + attrParas[0]);
                    if (!attrParas[2].matches("^\\(.*\\)$"))  //not in char (x) form
                        throw new SnytaxError( "Wrong definition of char length in " + attrParas[0]);

                    attrLength = attrParas[2].substring(1, attrParas[2].length() - 1); //get length
                    try {
                        length = Integer.parseInt(attrLength); //check the length
                    } catch (NumberFormatException e) {
                        throw new SnytaxError(  "The char length in " + attrParas[0] + " dosen't match a int type or overflow");
                    }
                    if (length < 1 || length > 255)
                        throw new SnytaxError( "The char length in " + attrParas[0] + " must be in [1,255] ");
                    endIndex = 3; //expected end index
                } else { //unmatched type
                    throw new SnytaxError(  "Error attribute type " + attrType + " in " + attrParas[0]);
                }

                if (attrParas.length == endIndex) { //check unique constraint
                    attrUnique = false;
                } else if (attrParas.length == endIndex + 1 && attrParas[endIndex].equals("unique")) {  //unique
                    attrUnique = true;
                } else { //wrong definition
                    throw new SnytaxError( "Error constraint definition in " + attrParas[0]);
                }

                if (attrType.equals("char")) { //generate attribute
                    attribute = new Attribute(attrName, NumType.valueOf(attrType.toUpperCase()), Integer.parseInt(attrLength), attrUnique);
                } else {
                    attribute = new Attribute(attrName, NumType.valueOf(attrType.toUpperCase()),1, attrUnique);
                }
                attrVec.add(attribute);
            }
        }

        if (primaryName.equals(""))  //check whether set the primary key
            throw new SnytaxError(  "Not specified primary key in table " + tableName);

        Table table = new Table(tableName, primaryName, attrVec); // create table
        API.create_table(tableName, table);
        System.out.println(">>>Create table " + tableName + " successfully");
    }

    private static void parse_drop_table(String statement) throws Exception {
        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new SnytaxError(  "Not specify table name");
        if (tokens.length != 3)
            throw new SnytaxError(  "Extra parameters in drop table");

        String tableName = tokens[2]; //get table name
        API.drop_table(tableName);
        System.out.println(">>>Drop table " + tableName + " successfully");
    }

    private static void parse_create_index(String statement) throws Exception {
        statement = statement.replaceAll("\\s+", " ");
        statement = statement.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
        statement = statement.trim();

        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new SnytaxError(  "Not specify index name");

        String indexName = tokens[2]; //get index name
        if (tokens.length == 3 || !tokens[3].equals("on"))
            throw new SnytaxError( "Must add keyword 'on' after index name " + indexName);
        if (tokens.length == 4)
            throw new SnytaxError(  "Not specify table name");

        String tableName = tokens[4]; //get table name
        if (tokens.length == 5)
            throw new SnytaxError( "Not specify attribute name in table " + tableName);

        String attrName = tokens[5];
        if (!attrName.matches("^\\(.*\\)$"))  //not as (xxx) form
            throw new SnytaxError(  "Error in specifiy attribute name " + attrName);

        attrName = attrName.substring(1, attrName.length() - 1); //extract attribute name
        if (tokens.length != 6)
            throw new SnytaxError(  "Extra parameters in create index");
        if (!CatalogManager.isUnique(tableName, attrName))
            throw new RunTimeError( "Not a unique attribute");

        Index index = new Index(indexName, tableName, attrName);
        API.create_index(index);
        System.out.println(">>>Create index " + indexName + " successfully");
    }

    private static void parse_drop_index(String statement) throws Exception {
        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new SnytaxError(  "Not specify index name");
        if (tokens.length != 3)
            throw new SnytaxError( "Extra parameters in drop index");

        String indexName = tokens[2]; //get table name
        API.drop_index(indexName);
        System.out.println(">>>Drop index " + indexName + " successfully");
    }

    private static void parse_select(String statement) throws Exception {
        //select ... from ... where ...
        String attrStr = utils.substring(statement, "select ", " from");
        String tabStr = utils.substring(statement, "from ", " where");
        String conStr = utils.substring(statement, "where ", "");
        Vector<Condition> conditions;
        Vector<String> attrNames;
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        if (attrStr.equals(""))
            throw new SnytaxError( "Can not find key word 'from' or lack of blank before from!");
        if (attrStr.trim().equals("*")) {
            //select all attributes
            if (tabStr.equals("")) {  // select * from [];
                tabStr = utils.substring(statement, "from ", "");
                Vector<TableRow> ret = API.select(tabStr, new Vector<>(), new Vector<>());
                endTime = System.currentTimeMillis();
                utils.print_rows(ret, tabStr);
            } else { //select * from [] where [];
                String[] conSet = conStr.split(" *and *");
                //get condition vector
                conditions = utils.create_conditon(conSet);
                Vector<TableRow> ret = API.select(tabStr, new Vector<>(), conditions);
                endTime = System.currentTimeMillis();
                utils.print_rows(ret, tabStr);
            }
        } else {
            attrNames = utils.convert(attrStr.split(" *, *")); //get attributes list
            if (tabStr.equals("")) {  //select [attr] from [];
                tabStr = utils.substring(statement, "from ", "");
                Vector<TableRow> ret = API.select(tabStr, attrNames, new Vector<>());
                endTime = System.currentTimeMillis();
                utils.print_rows(ret, tabStr);
            } else { //select [attr] from [table] where
                String[] conSet = conStr.split(" *and *");
                //get condition vector
                conditions = utils.create_conditon(conSet);
                Vector<TableRow> ret = API.select(tabStr, attrNames, conditions);
                endTime = System.currentTimeMillis();
                utils.print_rows(ret, tabStr);
            }
        }
        double usedTime = (endTime - startTime) / 1000.0;
        System.out.println("Finished in " + usedTime + " s");
    }

    private static void parse_insert(String statement) throws Exception {
        statement = statement.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
        statement = statement.replaceAll(" *, *", ",");
        statement = statement.trim();
        statement = statement.replaceAll("^insert", "").trim();  //skip insert keyword

        int startIndex, endIndex;
        if (statement.equals(""))
            throw new SnytaxError(  "Must add keyword 'into' after insert ");

        endIndex = statement.indexOf(" "); //check into keyword
        if (endIndex == -1)
            throw new SnytaxError( "Not specify the table name");
        if (!statement.substring(0, endIndex).equals("into"))
            throw new SnytaxError(  "Must add keyword 'into' after insert");

        startIndex = endIndex + 1;
        endIndex = statement.indexOf(" ", startIndex); //check table name
        if (endIndex == -1)
            throw new SnytaxError(  "Not specify the insert value");

        String tableName = statement.substring(startIndex, endIndex); //get table name
        startIndex = endIndex + 1;
        endIndex = statement.indexOf(" ", startIndex); //check values keyword
        if (endIndex == -1)
            throw new SnytaxError(  "Syntax error: Not specify the insert value");

        if (!statement.substring(startIndex, endIndex).equals("values"))
            throw new SnytaxError(  "Must add keyword 'values' after table " + tableName);

        startIndex = endIndex + 1;
        if (!statement.substring(startIndex).matches("^\\(.*\\)$"))  //check brackets
            throw new SnytaxError(  "Can't not find the insert brackets in table " + tableName);

        String[] valueParas = statement.substring(startIndex + 1).split(","); //get attribute tokens
        TableRow tableRow = new TableRow();

        for (int i = 0; i < valueParas.length; i++) {
            if (i == valueParas.length - 1)  //last attribute
                valueParas[i] = valueParas[i].substring(0, valueParas[i].length() - 1);
            if (valueParas[i].equals("")) //empty attribute
                throw new SnytaxError( "Empty attribute value in insert value");
            if (valueParas[i].matches("^\".*\"$") || valueParas[i].matches("^\'.*\'$"))  // extract from '' or " "
                valueParas[i] = valueParas[i].substring(1, valueParas[i].length() - 1);
            tableRow.addValue(valueParas[i]); //add to table row
        }

        //System.out.println(CatalogManager.getAttrNum(tableName));

        //Check unique attributes
        if (tableRow.getSize() != CatalogManager.getAttrNum(tableName))
            throw new RunTimeError(  "Attribute number doesn't match");
        Vector<Attribute> attributes = CatalogManager.get_table(tableName).attribute;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = attributes.get(i);
            if (attr.isUnique) {
                Condition cond = new Condition(attr.attriName, "=", valueParas[i]);
                if (CatalogManager.isIndexKey(tableName, attr.attriName)) {
                    Index idx = CatalogManager.getIndex(CatalogManager.get_index_name(tableName, attr.attriName));
                    if (IndexManager.select(idx, cond).isEmpty())
                        continue;
                } else {
                    Vector<Condition> conditions = new Vector<>();
                    conditions.add(cond);
                    Vector<TableRow> res = RecordManager.select(tableName, conditions); //Supposed to be empty
                    if (res.isEmpty())
                        continue;
                }
                throw new RunTimeError(  "Duplicate unique key: " + attr.attriName);
            }
        }

        API.insert_row(tableName, tableRow);
        System.out.println(">>>Insert successfully");
    }

    private static void parse_delete(String statement) throws Exception {
        //delete from [tabName] where []
        int num;
        String tabStr = utils.substring(statement, "from ", " where").trim();
        String conStr = utils.substring(statement, "where ", "").trim();
        Vector<Condition> conditions;
        Vector<String> attrNames;
        if (tabStr.equals("")) {  //delete from ...
            tabStr = utils.substring(statement, "from ", "").trim();
            num = API.delete_row(tabStr, new Vector<>());
            System.out.println("Query ok! " + num + " row(s) are deleted");
        } else {  //delete from ... where ...
            String[] conSet = conStr.split(" *and *");
            //get condition vector
            conditions = utils.create_conditon(conSet);
            num = API.delete_row(tabStr, conditions);
            System.out.println("Query ok! " + num + " row(s) are deleted");
        }
    }

    private static void parse_quit(String statement, BufferedReader reader) throws Exception {
        String[] tokens = statement.split(" ");
        if (tokens.length != 1)
            throw new SnytaxError("Extra parameters in quit");

        API.store();
        reader.close();
        System.out.println("The miniSQL is Stopped~");
        System.exit(0);
    }

    private static void parse_sql_file(String statement) throws Exception {
        execFile++;
        String[] tokens = statement.split(" ");
        if (tokens.length != 2)
            throw new SnytaxError( "Extra parameters in sql file execution");

        String fileName = tokens[1];
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
            interpret(fileReader);
        } catch (FileNotFoundException e) {
            throw new RunTimeError("Can't find the file");
        } catch (IOException e) {
            throw new RunTimeError("IO exception occurs");
        } finally {
            execFile--;
        }
    }

    public static void main(String[] args) {
        try {
            API.Init();
            System.out.println("******************************************");
            System.out.println("************Welcome to miniSQL!***********");
            System.out.println("******************************************");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            interpret(reader);
        }
        catch (IOException e) {
            System.out.println("Run time error->IO Wrong");
        }
        catch (Exception e) {
            System.out.println("Default error->" + e.getMessage());
        }
    }
}