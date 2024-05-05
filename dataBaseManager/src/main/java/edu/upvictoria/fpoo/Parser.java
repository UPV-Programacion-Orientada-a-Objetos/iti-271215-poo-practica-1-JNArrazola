package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.SQLOutput;
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

        System.out.println(query);
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
                System.out.println("this is a insert");
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
            if (query.charAt(i) == ' ' || query.charAt(i) == '(') {
                index = i;
                break;
            }
            tableName += query.charAt(i);
        }

        String columnsStr = "";

        for (int i = index; i < query.length(); i++) {

            if ((i == query.length() - 1 && query.charAt(i) == ')') ||
                    columnsStr.isEmpty() && (query.charAt(i) == '('))
                continue;

            columnsStr += query.charAt(i);
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
        for(String s : brkQuery) System.out.println(s);

        for(String s : brkQuery){
            String name = "", length = "", type = "";
            int isNull = 0;

            String[] brokenSentence = s.split(" ");

            if(brokenSentence.length == 0){
                System.out.println("Sintaxis incorrecta");
                return;
            }

            // Name asignation
            try{
                name = brokenSentence[0];

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
                            type.equalsIgnoreCase("FLOAT")||
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

            // Validations
            if(Utilities.isReservedWord(name)||Utilities.isType(name)){
                System.out.println("El nombre de una columna no puede tener palabras reservadas");
                return;
            }

            if(!Utilities.isType(type)){
                System.out.println(type);
                System.out.println("No es un tipo válido");
                return;
            }

            try{
                Integer.parseInt(length);
            } catch (NumberFormatException e){
                System.out.println("Longitud inválida");
                return;
            }
            // Validations

            System.out.println(name);
            System.out.println(length);
            System.out.println(type);
        }
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

        if(name.isEmpty()||name.contains(".csv")) {
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
