package edu.upvictoria.fpoo;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatCodePointException;
import java.util.zip.DataFormatException;

/**
 * Function that parses the query to be able to distinguish between different types of them
 *
 * @author Joshua Arrazola
 */
public class Parser {
    private static final BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Function to parse the query, and start dividing it to see what type it is and filter if there's a typo
     */
    public static String parseQuery(String query) throws Exception {
        String brokeStr[] = query.split(" ");

        if (brokeStr.length <= 1 || !parenthesisCheck(query))
            throw new RuntimeException("Query incompleta");

        if (brokeStr[0].equalsIgnoreCase("USE")) {
            return FileManagement.useDatabase(query, brokeStr);
        } else if (brokeStr[0].equalsIgnoreCase("UPDATE")) { // TODO: Update
            System.out.println("this is an update");
        } else if (brokeStr[0].equalsIgnoreCase("DELETE") && brokeStr[1].equalsIgnoreCase("FROM")) { // TODO: Delete from
            System.out.println("this is a delete");
        } else if (brokeStr[0].equalsIgnoreCase("SELECT")) { // TODO: Select
            System.out.println("this is a select");
        } else if (brokeStr[0].equalsIgnoreCase("INSERT") && brokeStr[1].equalsIgnoreCase("INTO")) { // TODO: Insert into
            insertInto(query);
        } else if (brokeStr[0].equalsIgnoreCase("CREATE") && brokeStr[1].equalsIgnoreCase("TABLE")) { // TODO: Create table
            return createTable(query);
        } else if (brokeStr[0].equalsIgnoreCase("SHOW") && brokeStr[1].equalsIgnoreCase("TABLES")) { // Show table WORKING
            return showTables(query, brokeStr);
        } else if (brokeStr[0].equalsIgnoreCase("DROP") && brokeStr[1].equalsIgnoreCase("TABLE")) {
            return dropTable(query, brokeStr);
        }

        return "No se reconoció la sentencia";
    }

    private static File[] obtainTableList() {
        File files = new File(FileManagement.getDatabasePath());
        return files.listFiles();
    }

    /**
     * Function to manage show tables query
     */
    private static String showTables(String query, String[] brokeStr) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new NullPointerException("No hay path designado");

        if (brokeStr.length > 2)
            throw new Exception("Sintaxis incorrecta");

        File[] files = obtainTableList();

        try {
            if (files.length == 0)
                return "No hay archivos para listar";
        } catch (NullPointerException e) {
            throw new NullPointerException("El directorio no se encontró");
        }

        for (File file : files) {
            String name = file.getName();
            if (name.contains("aux"))
                continue;
            if(file.getName().contains(".csv")&&!file.getName().contains("~"))
                System.out.println("* " + name.substring(0, name.indexOf(".csv")));
        }
        return "Listado éxitoso";
    }

    /**
     * Function to manage and also parse CREATE TABLE query
     */
    private static String createTable(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new NullPointerException("No hay path asignado");

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
            throw new IndexOutOfBoundsException("Sintaxis equivocada");
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
        if (!Utilities.hasValidChars(tableName)) {
            throw new IllegalArgumentException("La entrada no tiene caracteres válidos");
        } else if (!FileManagement.verifyDuplicatesTableName(tableName)) {
            throw new IllegalArgumentException("La entrada ya existe");
        }

        String columnsStr = "";
        for (int i = index; i < query.length(); i++) {

            if ((i == query.length() - 1 && query.charAt(i) == ')') ||
                    columnsStr.isEmpty() && (query.charAt(i) == '('))
                continue;

            columnsStr += query.charAt(i);
        }

        if (columnsStr.isEmpty())
            throw new IllegalArgumentException("Faltan argumentos (columnas)");


        columnsStr = columnsStr.trim();
        ArrayList<String> parseCreateRequest = new ArrayList<String>();

        String formedWord = "";
        for (int i = 0; i < columnsStr.length(); i++) {
            if (columnsStr.charAt(i) != ' ') {
                formedWord += columnsStr.charAt(i);

                if (i == columnsStr.length() - 1)
                    parseCreateRequest.add(formedWord.trim());
            } else if (!formedWord.isEmpty()) {
                parseCreateRequest.add(formedWord.trim());
                formedWord = "";
            }
        }

        String midProcessing = "";
        for (String S : parseCreateRequest) midProcessing += (S + " ");


        String[] brkQuery = midProcessing.split(",");

        for (int i = 0; i < brkQuery.length; i++) brkQuery[i] = brkQuery[i].trim();

        ArrayList<TypeBuilder> types = new ArrayList<TypeBuilder>();
        boolean primaryKey = false;
        for (String s : brkQuery) {
            String name = "", length = "", type = "";
            boolean isPrimaryKey = false, canBeNull = true;

            String[] brokenSentence = s.split(" ");

            if (brokenSentence.length == 0)
                throw new IllegalArgumentException("Sintaxis incorrecta");

            // Name asignation
            try {
                name = brokenSentence[0];

                if (!Utilities.hasValidChars(name))
                    throw new IllegalArgumentException("Nombres de columna inválidos");

                // Determine type of the row
                if (brokenSentence[1].contains("(")) {
                    String t = "";
                    String l = "";
                    boolean flag = false;

                    for (int i = 0; i < brokenSentence[1].length(); i++) {
                        if (brokenSentence[1].charAt(i) == '(' || flag) {
                            if (Character.isDigit(brokenSentence[1].charAt(i)))
                                l += brokenSentence[1].charAt(i);

                            flag = true;
                        } else
                            t += brokenSentence[1].charAt(i);
                    }
                    length = l;
                    type = t;

                    if (type.equalsIgnoreCase("INT") ||
                            type.equalsIgnoreCase("DATE"))
                            throw new IllegalArgumentException("No se le puede añadir precisión a estos tipos de datos");

                } else {
                    type = brokenSentence[1];
                    length = "-1";
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Sintaxis incorrecta");
            }
            // Ending of determine type block

            // Validations
            if (Utilities.isReservedWord(name) || Utilities.isType(name))
                throw new IllegalArgumentException("El nombre no puede tener palabras reservadas");


            if (!Utilities.isType(type))
                throw new IllegalArgumentException("El nombre no puede ser un tipo de dato");

            try {
                Integer parse = Integer.parseInt(length);
                if (parse != -1 && parse <= 0) throw new NumberFormatException("Longitud inválida");
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Argumento de longitud incorrecto");
            }

            // Validations
            try {
                if (brokenSentence[2].equalsIgnoreCase("NOT") && brokenSentence[3].equalsIgnoreCase("NULL"))
                    canBeNull = false;
                else {
                    throw new RuntimeException("Sintaxis inválida");
                }
            } catch (IndexOutOfBoundsException ignored) {
            } catch (RuntimeException e) {
                throw new RuntimeException();
            }

            try {
                if (brokenSentence[4].equalsIgnoreCase("PRIMARY") && brokenSentence[5].equalsIgnoreCase("KEY")) {
                    if (primaryKey)
                        throw new RuntimeException("No puede haber dos primary keys");
                    isPrimaryKey = true;
                    primaryKey = true;
                } else
                    throw new RuntimeException("Sintaxis incorrecta");

            } catch (IndexOutOfBoundsException ignored) {
            } catch (RuntimeException e) {
                throw new RuntimeException();
            }

            types.add(new TypeBuilder(name, canBeNull, type, Integer.parseInt(length), isPrimaryKey));
        }

        if (!primaryKey)
            throw new RuntimeException("Sintaxis incorrecta");

        FileManagement.createFileTable(tableName, types);

        return "Tabla creada correctamente";
    }

    /**
     * Function to manage drop table function
     */
    private static String dropTable(String query, String[] brokeStr) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new NullPointerException("No hay path asignado");

        if (brokeStr.length > 3)
            throw new IllegalArgumentException("Sintaxis incorrecta");

        String name;
        name = brokeStr[2];

        if (name == null || name.isEmpty() || name.contains(".csv") || name.contains(".txt"))
            throw new FileNotFoundException("No se encontró el archivo");

        String auxFile = name + "_aux.txt";
        name += ".csv";
        File[] files = obtainTableList();

        for (File file : files)
            if (file.getName().equalsIgnoreCase(name)) {
                System.out.println("¿Seguro que deseas borrar la tabla(y/n)?");

                String answ;
                try {
                    answ = bf.readLine();

                    if (answ.equalsIgnoreCase("y")) {
                        file.delete();
                        new File(FileManagement.getDatabasePath() + auxFile).delete();
                        return "Tabla borrada éxitosamente";
                    } else {
                        return "Tabla no eliminada";
                    }

                } catch (IOException e) {
                    throw new IOException("No se pudo borrar la tabla");
                }

            }

        return "No se encontró la tabla";
    }

    public static void insertInto(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null) {
            System.out.println("No hay ninguna base de datos seleccionada");
            return;
        }

        String[] parts = query.split("\\s+(?=\\([^)]*\\))|\\s+(?![^(]*\\))");

        for (int i = 0; i < parts.length; i++)
            parts[i] = parts[i].replaceAll("\\(", "").replaceAll("\\)", "");

        if(parts.length > 6)
            throw new RuntimeException("Query inválida");

        String name = parts[2];

        if(!FileManagement.searchForTable(name))
                throw new FileNotFoundException("No se encontró el archivo");

        String columns = parts[3];

        if(!parts[4].equalsIgnoreCase("VALUES"))
            throw new IllegalArgumentException("Sintaxis no válida");

        String values = parts[5];

        String[] colBrk = columns.split(",");
        String[] valBrk = values.split(",");

        if(colBrk.length!=valBrk.length)
            throw new RuntimeException("Sintaxis incorrecta");

        for (int i = 0; i < colBrk.length; i++) {
            colBrk[i] = colBrk[i].trim();
            valBrk[i] = valBrk[i].trim();
        }

        ArrayList<TypeBuilder> types = FileManagement.decompressInfo(name);

        String header;
        try (BufferedReader br = new BufferedReader(new FileReader(FileManagement.getDatabasePath() + name + ".csv"))){
            header = br.readLine();
        } catch (IOException e){
            throw new IOException("No se pudo abrir el archivo");
        }

        String[] headerBrk = header.split(",");

       /* for (int i = 0; i < headerBrk.length; i++) {
            boolean f = false;
            for (int j = 0; j < colBrk.length; j++) {
                if(colBrk[j].equals(headerBrk[i])){
                    f = true;
                    break;
                }
            }

            if(!f)
                throw new RuntimeException("Nombre de columna no encontrado");
        }*/

        if(header.isEmpty())
            throw new RuntimeException("Ocurrió un error al leer la base de datos");

        String headerBuilder = "";

        for (int i = 0; i < headerBrk.length; i++) {
            boolean flag = false;
            for (int j = 0; j < colBrk.length; j++) {
                if(colBrk[j].equalsIgnoreCase(headerBrk[i])){
                    headerBuilder += valBrk[j] + ",";
                    flag = true;
                    break;
                }
            }
            if(!flag)
                headerBuilder += "null,";
        }
        headerBuilder = headerBuilder.substring(0, headerBuilder.length() - 1);

        String[] headerBrkValues = headerBuilder.split(",");

        for (int i = 0; i < headerBrkValues.length; i++) {
            for (int j = 0; j < types.size(); j++) {
                
            }
        }
    }

    public static boolean checkType(TypeBuilder T, String value) throws Exception {
        switch (T.getDataType()){
            case "int":
                try {
                    Integer p = Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException e){
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
            case "float":
                try{
                    Float p = Float.parseFloat(value);
                } catch (NumberFormatException e){
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                if(T.getLength()!=-1&&value.length() - 1 - value.indexOf(".") > T.getLength())
                    throw new NumberFormatException("Precisión equivocada");

                return true;

            case "double":
                try{
                    Double p = Double.parseDouble(value);
                } catch (NumberFormatException e){
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                if(T.getLength()!=-1&&value.length() - 1 - value.indexOf(".") > T.getLength())
                    throw new NumberFormatException("Precisión equivocada");
                return true;
            case "varchar":
                if(value.charAt(0)!='\''&&value.charAt(value.length()-1)!='\'')
                    throw new NumberFormatException("Tipo de dato incorrecto");
                if(T.getLength()!=-1&&value.length() > T.getLength())
                    throw new Exception("Mayor longitud");
                return true;
        }

        return false;
    }




    /**
     * Function to validate parenthesis
     *
     * @return boolean
     */
    public static boolean parenthesisCheck(String query) {
        int parenthesis = 0;
        for (int i = 0; i < query.length(); i++) {
            switch (query.charAt(i)) {
                case '(':
                    parenthesis++;
                    break;
                case ')':
                    parenthesis--;
                    if (parenthesis < 0) return false;
                    break;
            }
        }

        return parenthesis == 0;
    }
}
