package edu.upvictoria.fpoo;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

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
        Utilities.fillReservedWords();
        Utilities.fillTypes();

        File file = new File(folderPath);
        if(!file.exists()){
            file.mkdir();
        }
    }

    /**
     * Function to manage Create Database query
     * */
    public static void createDatabase(String query, String[] brkQuery){
            if(brkQuery.length>3){
                System.out.println("SINTAXIS INCORRECTA");
                return;
            }
            String name = brkQuery[2];

            if(name.contains("/")){
                System.out.println("No se pueden utilizar caracteres / como nombre");
                return;
            }

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

    public static ArrayList<String> createDatatypeString(ArrayList<TypeBuilder> rows){
        ArrayList<String> auxFileCodification = new ArrayList<>();

        for(TypeBuilder row : rows){
            String coder = "";

            coder+=row.getName() + ",";
            coder+=row.getCanBeNull() + ",";
            coder+=row.getDataType() + ",";
            coder+=row.getLength() + ",";
            coder+=row.isPrimaryKey();

            auxFileCodification.add(coder);
        }

        return auxFileCodification;
    }

    public static void createFileTable(String tableName, ArrayList<TypeBuilder> rows){
        if(rows.isEmpty()){
            System.out.println("Query incompleto");
            return;
        }

        String headerTable = "";
        for (int i = 0; i < rows.size(); i++) {
            headerTable+=rows.get(i).getName();

            if(i!=rows.size()-1)
                headerTable+=",";
        }

        ArrayList<String> codec = createDatatypeString(rows);
        try(BufferedWriter bf = new BufferedWriter(new FileWriter(databasePath + "/" + tableName + ".csv"))){
            bf.write(headerTable);
            bf.newLine();
        } catch (IOException e){
            System.out.println("No se puede crear el archivo");
            return;
        }

        try(BufferedWriter bf = new BufferedWriter(new FileWriter(databasePath + "/" + tableName + "_aux.txt"))){
            for(String row : codec){
                bf.write(row);
                bf.newLine();
            }
        } catch (IOException e){
            System.out.println("No se pudo crear el archivo auxiliar");
            File file = new File(databasePath + "/" + tableName + ".csv");
            file.delete();
            return;
        }
    }
}
