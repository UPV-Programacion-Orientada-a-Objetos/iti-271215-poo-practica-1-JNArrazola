package edu.upvictoria.fpoo;

import java.io.File;

/**
 * Function that parses the query to be able to distinguish between different types of them
 * @author Joshua Arrazola
 * */
public class Parser {

    /**
     * Function to parse the query, and start dividing it to see what type it is and filter if there's a typo
     * */
    public static void parseQuery(String query){
        String brokeStr[] = query.split(" ");

        if(brokeStr.length <= 1){
            System.out.println("Query incompleta");
            return;
        }

        try{
            if(brokeStr[0].equalsIgnoreCase("UPDATE")){ // TODO: Update
                System.out.println("this is an update");
            } else if(brokeStr[0].equalsIgnoreCase("DELETE")&&brokeStr[1].equalsIgnoreCase("FROM")){ // TODO: Delete from
                System.out.println("this is a delete");
            } else if(brokeStr[0].equalsIgnoreCase("SELECT")){ // TODO: Select
                System.out.println("this is a select");
            } else if(brokeStr[0].equalsIgnoreCase("INSERT")&& brokeStr[1].equalsIgnoreCase("INTO")){ // TODO: Insert into
                System.out.println("this is a insert");
            } else if(brokeStr[0].equalsIgnoreCase("CREATE")&& brokeStr[1].equalsIgnoreCase("TABLE")){ // TODO: Create table
                // createTable(query);
                System.out.println("this is a create");
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
    private static void createTable(String query){
        String wordFormed = "";

        /**
         * move to the table name position;
         * */
        int index;
        try{
            for(index = 0; index < query.length(); index++){
                if(query.charAt(index)==' ')
                    wordFormed = "";

                wordFormed += query.charAt(index);

                if(wordFormed.equals("TABLE")){
                    ++index;
                }
            }
        } catch (IndexOutOfBoundsException e){
            System.out.println("Sintaxis equivocada");
            return;
        }

        System.out.println(query.charAt(index));
    }

    private static void dropTable(String query, String[] brokeStr){
        if(brokeStr.length > 3){
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
                file.delete();
                System.out.println("Tabla borrada");
                return;
            }


        System.out.println("No se borró ninguna tabla");
    }
}
