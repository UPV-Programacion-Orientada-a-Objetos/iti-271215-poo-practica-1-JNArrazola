package edu.upvictoria.fpoo;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.FileSystemException;
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
     * Funtion to manage Use Database query
     * */
    public static void useDatabase(String query, String[] brkQuery) throws Exception {
        if(brkQuery.length>2)
            throw new IndexOutOfBoundsException("Sentencia inválida");
        
        String path = brkQuery[1];
        
        if(!path.contains("/"))
            throw new FileNotFoundException("Ruta equivocada");
        

        File file = new File(path);
        
        if(!file.exists())
            try {
                file.mkdir();
            } catch (Exception e) {
                throw new FileSystemException("No se puede crear el archivo");
            }
        

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(folderAppPath, true))) {
            bw.write(path);
            bw.newLine();
        } catch (Exception e) {
            throw new FileSystemException("No se pudo escribir el archivo");
        }
    }

    // -------------------------------------------------
    //               Start: Getters and Setters
    // -------------------------------------------------


    public static String getDatabasePath() {
        return databasePath;
    }

    // -------------------------------------------------
    //               End: Getters and Setters
    // -------------------------------------------------

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

    public static boolean searchForTable(String name){
        File[] files = new File(databasePath).listFiles();
        for(File file : files)
            if(file.getName().equals(name+".csv"))
                return true;
        return false;
    }

    public static boolean verifyDuplicatesTableName(String name){
        File[] files = new File(databasePath).listFiles();

        try {
            for (File file : files) {
                String[] fileWords = file.getName().split("/");
                String fileName = fileWords[fileWords.length - 1];

                if (fileName.equals(name + ".csv"))
                    return false;

            }
        } catch (NullPointerException ignore){}

        return true;
    }

    public static ArrayList<TypeBuilder> decompressInfo(String name){
            ArrayList<TypeBuilder> rowsType = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new FileReader(databasePath + "/" + name + "_aux.txt"))){
                String line;
                while ((line = br.readLine()) != null) {
                    TypeBuilder tp = new TypeBuilder();

                    String[] ln = line.split(",");
                    tp.setName(ln[0]);
                    tp.setCanBeNull(Boolean.parseBoolean(ln[1]));
                    tp.setDataType(ln[2]);
                    tp.setLength(Integer.parseInt(ln[3]));
                    tp.setPrimaryKey(Boolean.parseBoolean(ln[4]));

                    rowsType.add(tp);
                }
            } catch (IOException e){
                System.out.println("Archivo no encontrado");
                return rowsType;
            }

        return rowsType;
    }

}
