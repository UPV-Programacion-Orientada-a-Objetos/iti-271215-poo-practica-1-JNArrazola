package edu.upvictoria.fpoo;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
            return select(query);
        } else if (brokeStr[0].equalsIgnoreCase("INSERT") && brokeStr[1].equalsIgnoreCase("INTO")) {
            return (insertInto(query));
        } else if (brokeStr[0].equalsIgnoreCase("CREATE") && brokeStr[1].equalsIgnoreCase("TABLE")) {
            return createTable(query);
        } else if (brokeStr[0].equalsIgnoreCase("SHOW") && brokeStr[1].equalsIgnoreCase("TABLES")) {
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
            if (file.getName().contains(".csv") && !file.getName().contains("~"))
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

    public static String insertInto(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new IOException("No hay ninguna base de datos seleccionada");

        String[] parts = query.split("\\s+(?=\\([^)]*\\))|\\s+(?![^(]*\\))");

        for (int i = 0; i < parts.length; i++)
            parts[i] = parts[i].replaceAll("\\(", "").replaceAll("\\)", "");

        if (parts.length > 6)
            throw new RuntimeException("Query inválida");

        String name = parts[2];

        if (!FileManagement.searchForTable(name))
            throw new FileNotFoundException("No se encontró el archivo");

        String columns = parts[3];

        if (!parts[4].equalsIgnoreCase("VALUES"))
            throw new IllegalArgumentException("Sintaxis no válida");

        String values = parts[5];

        String[] colBrk = columns.split(",");
        String[] valBrk = values.split(",");

        if (colBrk.length != valBrk.length)
            throw new RuntimeException("Sintaxis incorrecta");

        for (int i = 0; i < colBrk.length; i++) {
            colBrk[i] = colBrk[i].trim();
            valBrk[i] = valBrk[i].trim();
        }

        ArrayList<TypeBuilder> types = FileManagement.decompressInfo(name);

        String header;
        try (BufferedReader br = new BufferedReader(new FileReader(FileManagement.getDatabasePath() + name + ".csv"))) {
            header = br.readLine();
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        String[] headerBrk = header.split(",");

        if (header.isEmpty())
            throw new RuntimeException("Ocurrió un error al leer la base de datos");

        String headerBuilder = "";

        for (int i = 0; i < headerBrk.length; i++) {
            boolean flag = false;
            for (int j = 0; j < colBrk.length; j++) {
                if (colBrk[j].equalsIgnoreCase(headerBrk[i])) {
                    headerBuilder += valBrk[j] + ",";
                    flag = true;
                    break;
                }
            }
            if (!flag)
                headerBuilder += "null,";
        }
        headerBuilder = headerBuilder.substring(0, headerBuilder.length() - 1);

        String[] headerBrkValues = headerBuilder.split(",");

        // types es el tipo de cada uno de los valores de la columna
        // headerBrkValues son los valores de las columnas
        // headerBrk son los nombres de la columna


        for (int i = 0; i < headerBrkValues.length; i++)
            for (TypeBuilder type : types)
                if (type.getName().equalsIgnoreCase(headerBrk[i])) {
                    if (type.isPrimaryKey())
                        verifyUniqueness(type.getName(), name, headerBrkValues[i]);


                    checkType(type, headerBrkValues[i]);
                }

        String stringToWrite = "";
        for (int i = 0; i < headerBrkValues.length; i++)
            stringToWrite += headerBrkValues[i] + ",";
        stringToWrite = stringToWrite.substring(0, stringToWrite.length() - 1);

        try (BufferedWriter bf = new BufferedWriter(new FileWriter(FileManagement.getDatabasePath() + name + ".csv", true))) {
            bf.write(stringToWrite);
            bf.newLine();
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        return "Registro éxitoso";
    }

    public static boolean verifyUniqueness(String colName, String tableName, String value) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            String line = br.readLine();

            int index = 0;

            String[] lnBreak = line.split(",");
            for (int i = 0; i < lnBreak.length; i++) {
                if (lnBreak[i].equalsIgnoreCase(colName)) {
                    index = i;
                    break;
                }
            }

            while ((line = br.readLine()) != null) {
                String[] brkLine = line.split(",");

                if (brkLine[index].equalsIgnoreCase(value))
                    throw new IllegalArgumentException("PK repetida");

            }


        } catch (NumberFormatException e) {
            throw new NumberFormatException("La pk se repite");
        }
        return true;
    }

    public static boolean checkType(TypeBuilder T, String value) throws Exception {
        if (value.equalsIgnoreCase("null") && T.getCanBeNull())
            return true;
        else if (value.equalsIgnoreCase("null") && !T.getCanBeNull())
            throw new RuntimeException("Hay valores nulos que no deberían de serlo");

        switch (T.getDataType()) {
            case "int":
                try {
                    Integer p = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                return true;
            case "float":
                try {
                    Float p = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                if (T.getLength() != -1 && value.length() - 1 - value.indexOf(".") > T.getLength())
                    throw new NumberFormatException("Precisión equivocada");
                return true;

            case "double":
                try {
                    Double p = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                if (T.getLength() != -1 && value.contains(".") && value.length() - 1 - value.indexOf(".") > T.getLength())
                    throw new NumberFormatException("Precisión equivocada");
                return true;
            case "varchar":
                if (value.charAt(0) != '\'' && value.charAt(value.length() - 1) != '\'')
                    throw new NumberFormatException("Tipo de dato incorrecto");
                if (T.getLength() != -1 && value.length() > T.getLength())
                    throw new Exception("Mayor longitud");
                return true;
        }

        return false;
    }


    public static String select(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new FileNotFoundException("No se ha accedido a ninguna base de datos");

        ArrayList<String> words = new ArrayList<>();
        String formedString = "";
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == ' ' && query.charAt(i + 1) != ',') {
                words.add(formedString);
                formedString = "";
            } else
                formedString += query.charAt(i);
        }
        words.add(formedString);

        if (words.size() < 3)
            throw new RuntimeException("Sintaxis incorrecta");

        if (!words.get(0).equalsIgnoreCase("SELECT")
                || !words.get(2).equalsIgnoreCase("FROM"))
            throw new RuntimeException("Sintaxis incorrecta");

        String tableName = words.get(3);
        if (!FileManagement.searchForTable(tableName))
            throw new FileNotFoundException("No se encontró la tabla");

        String condiciones = "";

        if (query.contains("WHERE".toUpperCase())) {
            try {
                words.get(5);
            } catch (Exception IndexOutOfBounds) {
                throw new IndexOutOfBoundsException("Query inválida");
            }

            for (int i = 5; i < words.size(); i++)
                condiciones += words.get(i);

            manageWhere(condiciones, tableName);

        }
        return "Select realizado con éxito";
    }

    public static void manageWhere(String condiciones, String tableName) throws  Exception {
        condiciones = condiciones.replace(">=", "tempOne");
        condiciones = condiciones.replace("<=", "tempTwo");

        condiciones = condiciones.replace("AND", "&&");
        condiciones = condiciones.replace("and", "&&");
        condiciones = condiciones.replace("OR", "||");
        condiciones = condiciones.replace("or", "||");
        condiciones = condiciones.replace("<>", "!=");
        condiciones = condiciones.replace("'", "\"");
        condiciones = condiciones.replace("=", "==");
        condiciones = condiciones.replace("tempTwo", "<=");
        condiciones = condiciones.replace("tempOne", ">=");


        ArrayList<TypeBuilder> tp = FileManagement.decompressInfo(tableName);

        for (int i = 0; i < condiciones.length(); i++) {
            for (int j = 0; j < tp.size(); j++) {
                if (condiciones.contains(tp.get(j).getName())) {
                    TypeBuilder t = tp.get(j);

                    switch (tp.get(j).getDataType()) {
                        case "int":
                            condiciones = condiciones.replace(t.getName(), "Integer.parseInt(String.valueOf(arrBrk["+j+"]))");
                            break;
                        case "float":
                            condiciones = condiciones.replace(t.getName(), "Float.parseFloat(String.valueOf(arrBrk["+j+"]))");
                            break;
                        case "double":
                            condiciones = condiciones.replace(t.getName(), "Double.parseDouble(String.valueOf(arrBrk["+j+"]))");
                            break;
                    }
                }
            }
        }

        String tablaTemp = new File("").getAbsolutePath() + "/dataBaseManager/src/main/java/edu/upvictoria/fpoo/tablaTemp.java";
        String javacc = new File("").getAbsolutePath() + "/dataBaseManager/src/main/java/edu/upvictoria/fpoo/";
        String archivoTemp = new File("").getAbsolutePath() + "/dataBaseManager/src/main/java/edu/upvictoria/fpoo/database.txt";
        String archivoNombre = new File("").getAbsolutePath() + "/dataBaseManager/src/main/java/edu/upvictoria/fpoo/nombre.txt";

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemp))){
            bw.write(FileManagement.getDatabasePath());
        } catch(IOException e){
            throw new Exception("No se pudo crear el archivo");
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(archivoNombre))){
            bw.write(tableName);
        } catch(IOException e){
            throw new Exception("No se pudo crear el archivo");
        }

        /**
         * Write file
         * */
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(tablaTemp))){
            bw.write("package edu.upvictoria.fpoo;\n" +
                    "\n" +
                    "import java.io.*;\n" +
                    "import java.util.ArrayList;\n" +
                    "\n" +
                    "public class tablaTemp {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        String database = \"\";\n" +
                    "        String nombre = \"\";\n" +
                    "\n" +
                    "        String archivoNombre = new File(\"\").getAbsolutePath() + \"/dataBaseManager/src/main/java/edu/upvictoria/fpoo/nombre.txt\";\n" +
                    "        String archivoTemp = new File(\"\").getAbsolutePath() + \"/dataBaseManager/src/main/java/edu/upvictoria/fpoo/database.txt\";\n" +
                    "\n" +
                    "        try (BufferedReader bf = new BufferedReader(new FileReader(archivoNombre))) {\n" +
                    "            nombre = bf.readLine();\n" +
                    "        } catch (IOException e){};\n" +
                    "\n" +
                    "        try(BufferedReader bw = new BufferedReader(new FileReader(archivoTemp))){\n" +
                    "            database = bw.readLine();\n" +
                    "        }catch (IOException e){};\n" +
                    "\n" +
                    "        System.out.println(nombre);\n" +
                    "        System.out.println(database);\n" +
                    "\n" +
                    "        // arrBrk\n" +
                    "        ArrayList<String> tabla = new ArrayList<>();\n" +
                    "        try(BufferedReader br = new BufferedReader(new FileReader(database + nombre + \".csv\"))){\n" +
                    "            String line = br.readLine();\n" +
                    "            tabla.add(line);\n" +
                    "\n" +
                    "            // condicional\n" +
                    "            while((line = br.readLine()) != null){\n" +
                    "               String[] arrBrk = line.split(\",\");\n" +
                    "                if("+condiciones+")\n" +
                    "                   tabla.add(line);\n" +
                    "            }\n" +
                    "        }catch (Exception e){};\n" +
                    "\n" +
                    "        String file = new File(\"\").getAbsoluteFile() + \"/\";\n" +
                    "\n" +
                    "        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(\"\").getAbsolutePath()+\"/\" + nombre + \"doubleSelect.csv\"))){\n" +
                    "            for(String s : tabla){\n" +
                    "                bw.write(s); bw.newLine();};\n" +
                    "        } catch (Exception e){};\n" +
                    "    }\n" +
                    "}");
        } catch(IOException e){
            throw new Exception("No se pudo crear el archivo");
        }

        try {
            Process processOne = Runtime.getRuntime().exec("javac " + tablaTemp);
            processOne.waitFor(1000, TimeUnit.SECONDS);
            Process processTwo = Runtime.getRuntime().exec("java -classpath dataBaseManager/src/main/java/ edu.upvictoria.fpoo.tablaTemp");
            processTwo.waitFor(1000, TimeUnit.SECONDS);
        } catch (Exception e){
            throw new Exception("No se encontró el archivo");
        }

        (new File(tablaTemp)).delete();
        (new File(archivoNombre)).delete();
        (new File(archivoTemp)).delete();
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
