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

    // Path in which i will store the that contains the path to the database
    private static String folderPath = new File("").getAbsolutePath() + "/appPath";

    // Path that cointains the txt that guides to the entire database
    private static String folderAppPath = folderPath + "/path.txt";

    // path to the database
    private static String databasePath = null;

    /**
     * Function to validate initial conditions for the program
     * */
    public static void initialValidations(){
        boolean flag = false;

        System.out.println("Tienes que asignar un $PATH$ a la BBDD (comando USE $PATH$): ");
        // Set the paths
        do {
            if(checkFiles()) flag = true;
        }while (!flag);

        // Retrieve data
        retrievePath();
    }


    /**
     * Function to validate if there is a valid
     * @author Joshua Arrazola
     * @Returns boolean
     * */
    private static boolean checkFiles(){
        String f = new File("").getAbsolutePath();
        File pathFolder = new File(folderPath);

        // If it exists we just get outta of the function
        if(pathFolder.exists())
            return true;

        String query;
        try {
            System.out.println("Ingresa el query: ");
            query = bf.readLine();
        } catch (IOException e){
            System.out.println("Entrada equivocada");
            return false;
        }

        String pathForFile = "";
        try{
            String[] queryBrk = query.split(" ");

            if(queryBrk.length > 2) return false;

            if(queryBrk[0].equalsIgnoreCase("USE"))
                pathForFile = queryBrk[1];

            if(pathForFile.equals("")||pathForFile.contains(" ")) {
                System.out.println("Tienes que asignar el path a la BBDD");
                return false;
            }

        } catch (IndexOutOfBoundsException e){
            System.out.println("Sintaxis equivocada");
        }

        // I create the file in which im gonna store the path of the database system
        File appPathFile = new File(folderPath);
        appPathFile.mkdir();

        // So here I store the value of where is the database folder, and I also create the directory
        File databaseFolder = new File(pathForFile + "/database");
        databaseFolder.mkdirs();

        // I set the absolute path to write down there in the next try catch
        databasePath = new File("").getAbsolutePath() + "/" + pathForFile + "/database";

        // I write the absolute path to save the info of where im gonna store the database
        try (FileWriter fw = new FileWriter(folderPath + "/path.txt")) {
            fw.write(databasePath);
            System.out.println("Ruta de la base de datos escrita en el archivo correctamente.");
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo");
        } catch (IOException e){
            System.out.println("Ha ocurrido un error con el archivo: " + e.getMessage());
        }

        return true;
    }

    /**
     * Function to retrieve the path of the actual database to use it for other fuctions
     * @Returns String
     * @return databasePath
     * */
    private static String retrievePath(){
        try(BufferedReader bf = new BufferedReader(new FileReader(folderAppPath))){
            databasePath = bf.readLine();
        } catch (IOException e){
            System.out.println("No se pudo leer el archivo");
        }
        return databasePath;
    }

    // -------------------------------------------------
    //               Getters and Setters
    // -------------------------------------------------


    public static String getDatabasePath() {
        return databasePath;
    }
}
