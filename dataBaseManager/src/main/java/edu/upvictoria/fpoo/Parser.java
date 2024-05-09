package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        try {
            if (brokeStr.length <= 1 || !parenthesisCheck(query))
            throw new RuntimeException("Query incompleta");
            if (brokeStr[0].equalsIgnoreCase("USE")) {
                return FileManagement.useDatabase(query, brokeStr);
            } else if (brokeStr[0].equalsIgnoreCase("UPDATE")) { // TODO: Update
                // return update(query);
            } else if (brokeStr[0].equalsIgnoreCase("DELETE") && brokeStr[1].equalsIgnoreCase("FROM")) { // TODO: Delete from
                return deleteFrom(query);
            } else if (brokeStr[0].equalsIgnoreCase("SELECT")) { 
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
        } catch (Exception e) {
            throw new Exception(e.getMessage());
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

        if(tableName.equals(""))
            throw new IllegalArgumentException("Nombre de tabla vacío");
            
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
                if (value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'')
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
        
        // HashMap que contiene los alias
        HashMap<String, String> alias = new HashMap<>();

        // Args son todos los argumentos del select, condicionales guarda los condicionales
        String args = "", condicionales = "", tableName = "";
        String[] words = query.split(" ");
        int index = 0;
            
        for (int i = 1; i < words.length; i++) {
            if(words[i].equalsIgnoreCase("FROM")){
                index = i;
                break;
            }
            args += words[i] + " ";
        }

        // Aqui solo voy a guardar el header, no el alias
        String[] argsTrabajados = args.split(",");

        // Variable que contiene solo el header, sin alias
        String header = "";

        // Trabajar args
        for (int i = 0; i < argsTrabajados.length; i++) {
            argsTrabajados[i] = argsTrabajados[i].trim();

            // Trabajar alias
            String[] linea = argsTrabajados[i].split(" ");

            if(linea.length == 3)
                if(linea[2].charAt(0)=='\''&&linea[2].charAt(linea[2].length()-1)=='\''){
                    alias.put(linea[0], linea[2]);
                    header+=linea[0] + ",";
                }
                else 
                    throw new Exception("Alias incorrectos");
            else if(linea.length == 1)
                header+=linea[0] + ",";
            else
                throw new Exception("Sintaxis incorrecta");
        }
        header = header.substring(0, header.length()-1);


        if(index + 1 < words.length)
            tableName = words[++index];
        else throw new Exception("Sintaxis incorrecta");

        if(!FileManagement.searchForTable(tableName))
            throw new IOException("Tabla no encontrada");

        try {
            for (int i = index + 1; i < words.length; i++) {
                if(Utilities.isReservedWord(words[i])) continue;
                condicionales+=words[i] + " ";
            }
        } catch (Exception e) {
            throw new Exception("Sintaxis incorrecta");
        }

        // Header guarda los argumentos sin el alias
        // System.out.println(header);
        
        // Condicionales guarda el string de los condicionales
        // System.out.println(condicionales);

        // Alias guarda los alias
        // for(String key : alias.keySet())
        //     System.out.println(key + " " + alias.get(key));

        boolean containsWhere = false;
        if(query.contains("WHERE")||query.contains("where")){
            manageWhere(condicionales, tableName);
            containsWhere = true;
        } 

        String path = ((containsWhere) ? (new File("").getAbsolutePath()) + "/temporalAuxInfo.csv" : 
        FileManagement.getDatabasePath() + tableName + ".csv");

        if(header.equals("*")){
            try(BufferedReader br = new BufferedReader(new FileReader(path))){
                String line = br.readLine();
                String[] headerBrk = line.split(",");
    
                for (int i = 0; i < headerBrk.length; i++) {
                    if(alias.containsKey(headerBrk[i]))
                        System.out.print(alias.get(headerBrk[i]) + " ");
                    else
                        System.out.print(headerBrk[i] + " ");
                }
                System.out.println();
    
                while((line = br.readLine()) != null){
                    String[] arrBrk = line.split(",");
                    for (int i = 0; i < arrBrk.length; i++) {
                        System.out.print(arrBrk[i] + " ");
                    }
                    System.out.println();
                }
            } catch (IOException e){
                throw new IOException("Parámetros del select incorrectos");
            }
        } else {
            String[] headerBrk = header.split(",");
            try(BufferedReader br = new BufferedReader(new FileReader(path))){
                String line = br.readLine();
                String[] headerFile = line.split(",");
                ArrayList<Integer> indexes = new ArrayList<>();
    
                for (int i = 0; i < headerBrk.length; i++) {
                    for (int j = 0; j < headerFile.length; j++) {
                        if(headerBrk[i].equals(headerFile[j])){
                            indexes.add(j);
                            break;
                        }
                    }
                }
    
                for (int i = 0; i < headerBrk.length; i++) {
                    if(alias.containsKey(headerBrk[i]))
                        System.out.print(alias.get(headerBrk[i]) + " ");
                    else
                        System.out.print(headerBrk[i] + " ");
                }
                System.out.println();
    
                while((line = br.readLine()) != null){
                    String[] arrBrk = line.split(",");
                    for (int i = 0; i < indexes.size(); i++) {
                        System.out.print(arrBrk[indexes.get(i)] + " ");
                    }
                    System.out.println();
                }
            } catch (IOException e){
                throw new IOException("Parámetros del select incorrectos");
            }
        }

        

        if(containsWhere)
            Utilities.deleteFilesFromWhere();

        return "Select realizado con éxito";
    }

    public static void manageWhere(String condiciones, String tableName) throws  Exception {
        String path = FileManagement.getDatabasePath() + tableName + ".csv";
        
        String temp = "";
        for (int i = 0; i < condiciones.length(); i++) {
            if(condiciones.charAt(i)==' ') continue;
            temp+=condiciones.charAt(i);
        }
        condiciones = temp;
        
        // Regex para cambiar == a .equalsTo en strings
        condiciones = condiciones.replace("'", "\"");
        condiciones = condiciones.replace("=", "==");
        condiciones = condiciones.replace("<>", "!=");

        String regexOne = "==\\\"([^\\\"]*)\\\"";
        Pattern pattern = Pattern.compile(regexOne);
        Matcher matcher = pattern.matcher(condiciones);
        condiciones = matcher.replaceAll(".equals(\"\'$1\'\")");

        condiciones = condiciones.replace(">=", "tempOne");
        condiciones = condiciones.replace("<=", "tempTwo");
        condiciones = condiciones.replace("AND", "&&");
        condiciones = condiciones.replace("and", "&&");
        condiciones = condiciones.replace("OR", "||");
        condiciones = condiciones.replace("or", "||");
        condiciones = condiciones.replace("tempTwo", "<=");
        condiciones = condiciones.replace("tempOne", ">=");

        String header = "";
        
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            header = br.readLine();

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
                            case "varchar":
                                condiciones = condiciones.replace(t.getName(), "String.valueOf(arrBrk["+j+"])");
                                break;
                        }
                    }
                }
            }
        } catch (IOException e){
            throw new IOException("No se pudo abrir el archivo");
        }
        // Hasta acá está full trabajada la condición
        // Integer.parseInt(String.valueOf(arrBrk[0]))==10&&String.valueOf(arrBrk[1]).equals("miguel")
        // System.out.println(condiciones);
        
        // Path para mandarle la sentencia y el nombre de la tabla
        String temporalAuxInfoPath = (new File("")).getAbsolutePath() + "/temporalAuxInfo.txt";
        String temporalTableTemp = new File("").getAbsolutePath() + "/dataBaseManager/src/main/java/edu/upvictoria/fpoo/TablaTemp.java";
        String tableDir = FileManagement.getDatabasePath() + tableName + ".csv";

        try(BufferedWriter bf = new BufferedWriter(new FileWriter(temporalAuxInfoPath))){
            bf.write(tableDir);
        } catch (IOException e){
            throw new IOException("No se pudo abrir el archivo");
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(temporalTableTemp))){
            bw.write("package edu.upvictoria.fpoo;\n" + //
                                "\n" + //
                                "import java.io.BufferedReader;\n" + //
                                "import java.io.BufferedWriter;\n" + //
                                "import java.io.File;\n" + //
                                "import java.io.FileReader;\n" + //
                                "import java.io.FileWriter;\n" + //
                                "import java.util.ArrayList;\n" + //
                                "\n" + //
                                "public class TablaTemp {\n" + //
                                "    public static void main(String[] args) {\n" + //
                                "        String tableDir = \"\";\n" + //
                                "\n" + //
                                "        String temporalAuxInfoPath = (new File(\"\")).getAbsolutePath() + \"/temporalAuxInfo.txt\";\n" + //
                                "        try(BufferedReader br = new BufferedReader(new FileReader(temporalAuxInfoPath))){\n" + //
                                "            tableDir = br.readLine();\n" + //
                                "        } catch (Exception e){\n" + //
                                "            System.out.println(e.getMessage());\n" + //
                                "        }\n" + //
                                "\n" + //
                                "        ArrayList<String> tabla = new ArrayList<>();\n" + //
                                "        try(BufferedReader br = new BufferedReader(new FileReader(tableDir))){\n" + //
                                "            String line = br.readLine();\n" + //
                                "            tabla.add(line);\n" + //
                                "            while ((line = br.readLine()) != null) {\n" + //
                                "                String[] arrBrk = line.split(\",\"); \n" + //
                                "                if("+condiciones+")\n" + //
                                "                tabla.add(line);\n" + //
                                "            }\n" + //
                                "        } catch (Exception e){\n" + //
                                "            System.out.println(e.getMessage());\n" + //
                                "        }\n" + //
                                "\n" + //
                                "        try(BufferedWriter bw = new BufferedWriter(new FileWriter((new File(\"\")).getAbsolutePath() + \"/temporalAuxInfo.csv\"))){\n" + //
                                "            for(String row : tabla){\n" + //
                                "                bw.write(row);\n" + //
                                "                bw.newLine();\n" + //
                                "            }\n" + //
                                "        } catch (Exception e){\n" + //
                                "            System.out.println(e.getMessage());\n" + //
                                "        }\n" + //
                                "    }\n" + //
                                "}");
        } catch(IOException e){
            throw new Exception("No se pudo crear el archivo");
        }

        try {
            Process processOne = Runtime.getRuntime().exec("javac " + temporalTableTemp);
            processOne.waitFor(2000, TimeUnit.SECONDS);
            Process processTwo = Runtime.getRuntime().exec("java -classpath dataBaseManager/src/main/java/ edu.upvictoria.fpoo.TablaTemp");
            processTwo.waitFor(2000, TimeUnit.SECONDS);
        } catch (Exception e){
            throw new Exception("No se encontró el archivo");
        }
        Utilities.deleteFilesFromWhere();
    }

    public static String deleteFrom(String query) throws Exception {
        if(FileManagement.getDatabasePath() == null)
            throw new FileNotFoundException("No se ha accedido a ninguna base de datos");

        String[] words = query.split(" ");

        if(words.length<3) 
            throw new Exception("Sintaxis incorrecta");

        String tableName = words[2];

        if(!FileManagement.searchForTable(tableName))
            throw new Exception("No se ha encontrado el archivo;");

        String arguments = "";

        boolean flag = false;
        String condicionales = "";
        if(query.contains("WHERE")||query.contains("where")){

            for (int i = 3; i < words.length; i++) {
                if(Utilities.isReservedWord(words[i])) continue;
                condicionales+=words[i];
            }
            System.out.println(condicionales);
            flag = true;
        } else if(words.length > 3) throw new IllegalArgumentException("Sintaxis no válida");

        if(!flag){
            String path = FileManagement.getDatabasePath() + tableName;
            String header = "";

            try(BufferedReader bf = new BufferedReader(new FileReader(path + ".csv"))){
                header = bf.readLine();
            } catch(FileNotFoundException e){
                throw new FileNotFoundException("No se encontró el archivo");
            }

            try(BufferedWriter bw = new BufferedWriter(new FileWriter(path + ".csv"))){
                bw.write(header);
                bw.newLine();
            } catch(FileNotFoundException e){
                throw new FileNotFoundException("No se encontró el archivo");
            }
            return "Borrado éxitoso";
        }
        
        String path = (new File("").getAbsolutePath()) + "/temporalAuxInfo.csv";

        ArrayList<TypeBuilder> tp = FileManagement.decompressInfo(tableName);

        int indexPK = 0;
        for (int i = 0; i < tp.size(); i++) 
            if(tp.get(i).isPrimaryKey()){
                indexPK = i;
                break;
            }
        manageWhere(condicionales, tableName);
        
        HashSet<String> idsAExcluir = new HashSet<String>();

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while((line = br.readLine()) != null){
                String[] lineBrk = line.split(",");
                idsAExcluir.add(lineBrk[indexPK]);
            }
        } catch (FileNotFoundException e){
            throw new FileNotFoundException("No se encontró el archivo");
        }

        ArrayList<String> nuevaTabla = new ArrayList<>();

        String pathTableName = FileManagement.getDatabasePath() + tableName + ".csv";
        String header;
        try(BufferedReader bf = new BufferedReader(new FileReader(pathTableName))){
            header = bf.readLine();
            String line;
            while((line = bf.readLine()) != null){
                String[] lineBrk = line.split(",");
                if(!idsAExcluir.contains(lineBrk[indexPK]))
                    nuevaTabla.add(line);
            }
        } catch(FileNotFoundException e){
            throw new FileNotFoundException("No se encontró el archivo");
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(pathTableName))){
            bw.write(header);
            bw.newLine();

            for (int j = 0; j < nuevaTabla.size(); j++) {
                bw.write(nuevaTabla.get(j));
                bw.newLine();
            }
        }

        Utilities.deleteFilesFromWhere();
        return "Borrado éxitoso";
    }

    public static String update(String query) throws Exception{
        

        return "Update realizado con éxito";
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
