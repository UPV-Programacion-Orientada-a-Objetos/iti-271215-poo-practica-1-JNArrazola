package edu.upvictoria.fpoo;

import java.io.*;

/**
 * Class to manage files
 * @author Joshua Arrazola
 * @matricula 2230023
 * */
public class FileManagement {
    // Read without Scanner
    private static final BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    private static String appPath = new File("").getAbsolutePath() + "/";

    // Path in which i will store the that contains the path to the database
    private static String folderPath = new File("").getAbsolutePath() + "/appPath";

    // Path that cointains the txt that guides to the entire database
    private static String folderAppPath = folderPath + "/path.txt";

    // path to the database
    private static String databasePath = null;


    public static void initialValidations(){
        File file = new File(folderPath);

        if(!file.exists()){
            file.mkdir();
        }
    }


    /**
     * Function to retrieve the path of the actual database to use it for other fuctions
     * @Returns String
     * @return databasePath
     * *//*
    private static String retrievePath(){
        try(BufferedReader bf = new BufferedReader(new FileReader(folderAppPath))){
            databasePath = bf.readLine();
        } catch (IOException e){
            System.out.println("No se pudo leer el archivo");
        }
        return databasePath;
    }*/

    /**
     * Function to manage Create Database query
     * */
    public static void createDatabase(String query, String[] brkQuery){
            if(brkQuery.length>3){
                System.out.println("SINTAXIS INCORRECTA");
                return;
            }
            String name = brkQuery[2];

            File db = new File(appPath + name);

            if(db.exists()){
                System.out.println("La base de datos ya existe");
                return;
            }
            db.mkdir();

            try(BufferedWriter bw = new BufferedWriter(new FileWriter(folderAppPath, true))){
                bw.write(appPath + name);
                bw.newLine();
            } catch(IOException e){
                System.out.println("El archivo FOLDER PATH no existe, archivo creado");
                File f = new File(folderAppPath);
                f.mkdir();
            }

    }

    /**
     * Funtion to manage Use Database query
     * */
    public static void useDatabase(String query, String[] brkQuery){
        if(brkQuery.length>2){
            System.out.println("SINTAXIS INCORRECTA");
            return;
        }

        String name = brkQuery[1];

        File db = new File(appPath + name);

        if(!db.exists()){
            System.out.println("La base de datos no existe [utiliza el comando create table]");
            return;
        }

        System.out.println("Acceso a la base de datos éxitoso");
        databasePath = appPath + name;
    }

    // -------------------------------------------------
    //               Getters and Setters
    // -------------------------------------------------


    public static String getDatabasePath() {
        return databasePath;
    }
}
