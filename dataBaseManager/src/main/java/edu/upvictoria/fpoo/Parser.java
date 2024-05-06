package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Function that parses the query to be able to distinguish between different types of them
 * @author Joshua Arrazola
 * */
public class Parser {
    private static final BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Function to parse the query, and start dividing it to see what type it is and filter if there's a typo
     * */
    public static void parseQuery(String query){
        String brokeStr[] = query.split(" ");

        if(brokeStr.length <= 1||!parenthesisCheck(query)){
            System.out.println("Query incompleta");
            return;
        }

        try{
            if(brokeStr[0].equalsIgnoreCase("USE")){
                FileManagement.useDatabase(query, brokeStr);
            } else if(brokeStr[0].equalsIgnoreCase("CREATE")&&brokeStr[1].equalsIgnoreCase("DATABASE")){
                FileManagement.createDatabase(query, brokeStr);
            }
            else if(brokeStr[0].equalsIgnoreCase("UPDATE")){ // TODO: Update
                System.out.println("this is an update");
            } else if(brokeStr[0].equalsIgnoreCase("DELETE")&&brokeStr[1].equalsIgnoreCase("FROM")){ // TODO: Delete from
                System.out.println("this is a delete");
            } else if(brokeStr[0].equalsIgnoreCase("SELECT")){ // TODO: Select
                System.out.println("this is a select");
            } else if(brokeStr[0].equalsIgnoreCase("INSERT")&& brokeStr[1].equalsIgnoreCase("INTO")){ // TODO: Insert into
                insertInto(query);
            } else if(brokeStr[0].equalsIgnoreCase("CREATE")&&brokeStr[1].equalsIgnoreCase("TABLE")){ // TODO: Create table
                createTable(query);
            } else if(brokeStr[0].equalsIgnoreCase("SHOW")&&brokeStr[1].equalsIgnoreCase("TABLES")){ // Show table WORKING
                showTables(query, brokeStr);
            } else if(brokeStr[0].equalsIgnoreCase("DROP")&&brokeStr[1].equalsIgnoreCase("TABLE")){
                dropTable(query, brokeStr);
            }
        } catch (IndexOutOfBoundsException e){
            System.out.println("Query incompleta");
        } catch (Exception e){
            System.out.println("No se reconoció la query en cuestión");
        }
        return;
    }

    private static File[] obtainTableList(){
        File files = new File(FileManagement.getDatabasePath());
        return files.listFiles();
    }

    /**
     * Function to manage show tables query
     * */
    private static void showTables(String query, String[] brokeStr){
        if(FileManagement.getDatabasePath()==null){
            System.out.println("No hay path asignado");
            return;
        }

        if(brokeStr.length > 2){
            System.out.println("Sintaxis incorrecta");
            return;
        }

        File[] files = obtainTableList();

        try{
            if(files.length == 0){
                System.out.println("El directorio esta vacío");
            }
        } catch (NullPointerException e){
            System.out.println("El directorio no existe");
        }

        for(File file : files){
            String name = file.getName().substring(0, file.getName().indexOf("."));
            if(name.contains("aux"))
                continue;
            System.out.println(name);
        }
    }

    /**
     * Function to manage and also parse CREATE TABLE query
     * */
    private static void createTable(String query) {
        if (FileManagement.getDatabasePath() == null) {
            System.out.println("No hay path asignado");
            return;
        }

        String wordFormed = "";

        int index;
        try {
            for (index = 0; index < query.length(); index++) {
                if (query.charAt(index) == ' ') {
                    if (wordFormed.equals("TABLE")) {
                        ++index;
                        break;
                    }
                    wordFormed = "";
                } else if (query.charAt(index) != ' ')
                    wordFormed += query.charAt(index);


            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Sintaxis equivocada");
            return;
        }

        String tableName = "";

        for (int i = index; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                index = i;
                break;
            }
            tableName += query.charAt(i);
        }

        tableName = tableName.trim();
        if(!Utilities.hasValidChars(tableName)){
            System.out.println("Caracteres inválidos");
            return;
        } else if(!FileManagement.verifyDuplicatesTableName(tableName)){
            System.out.println("El nombre de la tabla está repetido");
            return;
        }

        String columnsStr = "";
        for (int i = index; i < query.length(); i++) {

            if ((i == query.length() - 1 && query.charAt(i) == ')') ||
                    columnsStr.isEmpty() && (query.charAt(i) == '('))
                continue;

            columnsStr += query.charAt(i);
        }

        if(columnsStr.isEmpty()){
            System.out.println("No hay argumentos");
            return;
        }

        columnsStr = columnsStr.trim();
        ArrayList<String> parseCreateRequest = new ArrayList<String>();

        String formedWord = "";
        for (int i = 0; i < columnsStr.length(); i++) {
            if (columnsStr.charAt(i) != ' '){
                formedWord += columnsStr.charAt(i);

                if(i == columnsStr.length()-1)
                    parseCreateRequest.add(formedWord.trim());
            }
            else if(!formedWord.isEmpty()){
                parseCreateRequest.add(formedWord.trim());
                formedWord = "";
            }
        }

        String midProcessing = "";
        for(String S : parseCreateRequest) midProcessing+=(S+" ");


        String[] brkQuery = midProcessing.split(",");

        for (int i = 0; i < brkQuery.length; i++) brkQuery[i] = brkQuery[i].trim();

        ArrayList<TypeBuilder> types = new ArrayList<TypeBuilder>();
        boolean primaryKey = false;
        for(String s : brkQuery){
            String name = "", length = "", type = "";
            boolean isPrimaryKey = false, canBeNull = true;

            String[] brokenSentence = s.split(" ");

            if(brokenSentence.length == 0){
                System.out.println("Sintaxis incorrecta");
                return;
            }

            // Name asignation
            try{
                name = brokenSentence[0];

                if(!Utilities.hasValidChars(name)){
                    System.out.println("Nombres de columna inválidos");
                    return;
                }

                // Determine type of the row
                if(brokenSentence[1].contains("(")){
                    String t = "";
                    String l = "";
                    boolean flag = false;

                    for (int i = 0; i < brokenSentence[1].length(); i++) {
                        if(brokenSentence[1].charAt(i) == '('||flag){
                            if(Character.isDigit(brokenSentence[1].charAt(i)))
                                l+=brokenSentence[1].charAt(i);

                            flag = true;
                        } else
                            t+=brokenSentence[1].charAt(i);
                    }
                    length = l;
                    type = t;
                    if(type.equalsIgnoreCase("INT")||
                    type.equalsIgnoreCase("DATE")){
                        System.out.println("No se puede añadir precisión a ese tipo de dato");
                        return;
                    }
                } else{
                    type = brokenSentence[1];
                    length = "-1";
                }
            } catch (IndexOutOfBoundsException e){
                System.out.println("Sintaxis incorrecta");
            }
            // Ending of determine type block

            // Validations
            if(Utilities.isReservedWord(name)||Utilities.isType(name)){
                System.out.println("El nombre de una columna no puede tener palabras reservadas");
                return;
            }

            if(!Utilities.isType(type)){
                System.out.println("No es un tipo válido");
                return;
            }

            try{
                Integer parse = Integer.parseInt(length);
                if(parse!=-1&&parse<=0) throw new NumberFormatException("Longitud inválida");
            } catch (NumberFormatException e){
                System.out.println(e.getMessage());
                return;
            }

            // Validations
            try{
                if(brokenSentence[2].equalsIgnoreCase("NOT")&&brokenSentence[3].equalsIgnoreCase("NULL"))
                    canBeNull = false;
                else {
                    throw new RuntimeException("Sintaxis inválida");
                }
            } catch (IndexOutOfBoundsException ignored){}
            catch (RuntimeException e){
                System.out.println(e.getMessage());
                return;
            }

            try{
                if(brokenSentence[4].equalsIgnoreCase("PRIMARY")&&brokenSentence[5].equalsIgnoreCase("KEY")){
                    if(primaryKey)
                        throw new RuntimeException("No puede haber dos primary keys");
                    isPrimaryKey = true;
                    primaryKey = true;
                } else
                    throw new RuntimeException("Sintaxis incorrecta");

            } catch (IndexOutOfBoundsException ignored){}
            catch (RuntimeException e){
                System.out.println(e.getMessage());
                return;
            }

            types.add(new TypeBuilder(name, canBeNull, type ,Integer.parseInt(length), isPrimaryKey));
        }

        if(!primaryKey){
            System.out.println("Se debe establecer una Primary Key");
            return;
        }

        FileManagement.createFileTable(tableName, types);
    }

    /**
     * Function to manage drop table function
     * */
    private static void dropTable(String query, String[] brokeStr){
        if(FileManagement.getDatabasePath()==null){
            System.out.println("No hay path asignado");
            return;
        }

        if(brokeStr.length > 3||query.contains("{")||query.contains("}")){
            System.out.println("Sintaxis incorrecta");
            return;
        }

        String name = "";

        try{
            name = brokeStr[2];
        } catch (IndexOutOfBoundsException e){
            System.out.println("Sintaxis equivocada");
            return;
        }

        if(name.isEmpty()||name.contains(".csv")||name.contains(".txt")) {
            System.out.println("Sintaxis incorrecta.");
            return;
        }

        name+=".csv";
        File[] files = obtainTableList();

        for(File file : files)
            if(file.getName().equalsIgnoreCase(name)){
                System.out.println("¿Seguro que deseas borrar la tabla(y/n)?");

                String answ;
                try{
                    answ = bf.readLine();

                    if(answ.equalsIgnoreCase("y")){
                        file.delete();
                        System.out.println("Tabla borrada éxitosamente");
                    } else {
                        System.out.println("Tabla no eliminada");
                    }

                } catch (IOException e){
                    System.out.println("Input Exception");
                }

                return;
            }
        System.out.println("No se borró ninguna tabla");
    }

    public static void  insertInto(String query){
        if(FileManagement.getDatabasePath() == null){
            System.out.println("No hay ninguna base de datos seleccionada");
            return;
        }

        int index = 0;
        String formedWord = "";
        boolean flag = false;
        for (int i = 0; i < query.length(); i++) {
            if(query.charAt(i) == ' '){
                if(formedWord.equalsIgnoreCase("INSERT")){
                    formedWord = "";
                }  else if(formedWord.equalsIgnoreCase("INTO")){
                    flag = true;
                    index = ++i;
                    break;
                }
            } else
                formedWord+=query.charAt(i);

        }

        if(!flag) {
            System.out.println("Query incompleta");
            return;
        }

        try{
           char test = query.charAt(index);
        } catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Sintaxis incorrecta");
        }

        String tableName = "";
        for (int i = index; i < query.length(); i++) {
            if(query.charAt(i) == ' '){
                insertAllValues(tableName, i + 1, query);
                return;
            } else if(query.charAt(i) == '('){
                insertSomeValues(tableName, i + 1, query);
                return;
            }
            tableName+=query.charAt(i);
        }

    }

    private static void insertAllValues(String name, int index, String query){
        if(!FileManagement.searchForTable(name)){
            System.out.println("Nombre de tabla inválido");
            return;
        }

        try{
            char test = query.charAt(index);
        }  catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Sintaxis incorrecta");
        }

        String into = "";
        for (int i = index; i < query.length(); i++) {
            if(query.charAt(i) == ' '){
                if(into.equalsIgnoreCase("VALUES")){
                    index = i;
                    break;
                }
                else {
                    System.out.println("Sintaxis incorrecta");
                    return;
                }
            }
            into+=query.charAt(i);
        }
        index++;

        try{
            char test = query.charAt(index);
        } catch (IndexOutOfBoundsException e){
            System.out.println("Sintaxis incorrecta");
            return;
        }

        if(query.charAt(index)!='('){
            System.out.println("Sintaxis incorrecta");
            return;
        }

        String types = "";
        for (int i = index; i < query.length(); i++)
            if(query.charAt(i)!='('&&query.charAt(i)!=')')
                types+=query.charAt(i);

        if (types.isEmpty()){
            System.out.println("Los tipos no pueden estar vacíos");
            return;
        }

        String[] entries = types.split(",");
        for (int i = 0; i < entries.length; i++)
            entries[i] = entries[i].trim();

        // Verify entries
        ArrayList<TypeBuilder> rows = FileManagement.decompressInfo(name);
        for (TypeBuilder row : rows)
            System.out.println(row.toString());

        // TODO: i decode data, now i have to validate entries and datatypes, also the number of entries has to be the same as the datatype
    }

    private static void insertSomeValues(String name, int index, String query){
        try{
            char test = name.charAt(index);
        }  catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Sintaxis incorrecta");
        }
    }


    /**
     * Function to validate parenthesis
     * @return boolean
     * */
    public static boolean parenthesisCheck(String query){
        int parenthesis = 0;
        for (int i = 0; i < query.length(); i++){
            switch (query.charAt(i)){
                case '(':
                    parenthesis++;
                    break;
                case ')':
                    parenthesis--;
                    if(parenthesis<0) return false;
                    break;
            }
        }

        return parenthesis == 0;
    }
}
