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
                System.out.println("THIS IS A CREATE");
            } else if(brokeStr[0].equalsIgnoreCase("SHOW")&&brokeStr[1].equalsIgnoreCase("TABLES")){ // Show table WORKING
                showTables(query, brokeStr);
            }
        } catch (IndexOutOfBoundsException e){
            System.out.println("Query incompleta");
        } catch (Exception e){
            System.out.println("No se reconoció la query en cuestión");
        }
        return;
    }

    /**
     * Function to manage show tables query
     * */
    private static void showTables(String query, String[] brokeStr){
        if(brokeStr.length > 2){
            System.out.println("Sintaxis incorrecta");
            return;
        }

        String path = FileManagement.getDatabasePath();
        File directory = new File(path);

        File[] files = directory.listFiles();

        try{
            if(files.length == 0){
                System.out.println("El directorio esta vacío");
            }
        } catch (NullPointerException e){
            System.out.println("El directorio no existe");
        }

        for(File file : files)
            System.out.println(file.getName());
    }
}
