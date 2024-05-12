package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Function that parses the query to be able to distinguish between different
 * types of them
 *
 * @author Joshua Arrazola
 */
public class Parser {
    private static final BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Function to parse the query, and start dividing it to see what type it is and
     * filter if there's a typo
     */
    public static String parseQuery(String query) throws Exception {
        String brokeStr[] = query.split(" ");

        try {
            if (brokeStr.length <= 1 || !parenthesisCheck(query))
                throw new RuntimeException("Query incompleta");
            if (brokeStr[0].equalsIgnoreCase("USE")) {
                return FileManagement.useDatabase(query, brokeStr);
            } else if (brokeStr[0].equalsIgnoreCase("UPDATE")) {
                return update(query);
            } else if (brokeStr[0].equalsIgnoreCase("DELETE") && brokeStr[1].equalsIgnoreCase("FROM")) {
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

        // Analizar primera parte del string (CREATE TABLE name)
        if (query.indexOf("(") == -1)
            throw new IllegalArgumentException("Sintaxis incorrecta");

        String firstPart = query.substring(0, query.indexOf("("));
        String[] firstPartBreak = firstPart.split(" ");

        // esto quiere decir que no se entiende donde esta el create table|
        if (firstPartBreak.length < 3)
            throw new Exception("Error de sintaxis");

        // Verificar las palabras
        String tableName = firstPartBreak[2];

        // Verificar que no exista el archivo, además verificar ciertas propiedades del
        // nombre
        if (FileManagement.searchForTable(tableName))
            throw new FileAlreadyExistsException("Nombre de tabla repetido");

        // Boolean function que valida el nombre
        Utilities.nameValidations(tableName);

        // Extraer los argumentos y separarlos
        String arguments = query.substring(query.indexOf("(") + 1, query.length() - 1);
        String[] argumentsBreak = arguments.split(",");

        for (int i = 0; i < argumentsBreak.length; i++)
            argumentsBreak[i] = argumentsBreak[i].trim();

        // Recorrer cada uno de los argumentos, se busca que al menos exista:
        // * Una y sólo una primary key
        // * Por lo menos un tipo de dato
        // * Que tenga un nombre la variable
        // * Si no se especifica not null, se asume que puede ser null
        // * Si es primary key no puede ser null
        // * No se le puede especificar longitud a una variable int o date

        HashSet<String> assignedColumnNames = new HashSet<>();
        ArrayList<TypeBuilder> typesOfTable = new ArrayList<>();

        boolean hasPK = false;
        try {
            for (String s : argumentsBreak) {
                String type = "", length = "";
                boolean canBeNull = true, isPk = false;

                String[] individualArgumentBreak = s.split(" ");

                // Check for name
                String columnName = individualArgumentBreak[0];
                Utilities.nameValidations(columnName);
                if (assignedColumnNames.contains(columnName))
                    throw new IllegalArgumentException("Nombre de columna repetido");

                // Check for type
                for (String substr : individualArgumentBreak) {
                    ArrayList<String> types = Utilities.getVectorOfDatatypes();

                    for (String t : types)
                        if (substr.toUpperCase().contains(t))
                            if (type.equals(""))
                                type = substr;
                            else
                                throw new IllegalArgumentException("No se pueden tener dos tipos de datos");

                }
                if (type.equals(""))
                    throw new IllegalArgumentException("No se encontró el tipo de dato");

                // Check for length
                // also check if the length is invalid
                if (type.contains("("))
                    length = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
                System.out.println(type);
                if (!(length.equalsIgnoreCase(""))
                        && (type.toLowerCase().contains("int") || type.toLowerCase().contains("date")))
                    throw new IllegalArgumentException("INT y DATE no pueden tener precisión");

                // try to parse length
                if (!length.equals(""))
                    try {
                        System.out.println("trueno");
                        int integer = Integer.parseInt(length);
                        if (integer != -1 && integer <= 0)
                            throw new NumberFormatException("Longitud inválida");
                    } catch (Exception e) {
                        throw new NumberFormatException("Longitud no válida");
                    }

                if (s.toUpperCase().contains("NOT NULL"))
                    canBeNull = false;
                else if (s.toUpperCase().contains("NULL"))
                    canBeNull = true;

                if (s.toUpperCase().contains("PRIMARY KEY"))
                    if (s.toUpperCase().contains("NULL") && !s.toUpperCase().contains("NOT NULL"))
                        throw new IllegalArgumentException("No puede ser PK y a la vez nula");
                    else if (hasPK)
                        throw new IllegalArgumentException("No se pueden tener dos PK");
                    else {
                        hasPK = true;
                        isPk = true;
                    }

                assignedColumnNames.add(columnName.trim());
                typesOfTable.add(new TypeBuilder(columnName.trim(), canBeNull,
                        ((length.equals("") ? type : type.substring(0, type.indexOf("(")))),
                        ((!length.equals("") ? Integer.parseInt(length) : -1)), isPk));
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Argumentos dentro de la creación de columnas inválidos");
        }

        if (!hasPK)
            throw new IllegalArgumentException("No se puede crear una tabla sin PK");
        FileManagement.createFileTable(tableName, typesOfTable);

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

        if (columns.equalsIgnoreCase("VALUES"))
            throw new IllegalArgumentException("Falta especificar columnas");

        if (!parts[4].equalsIgnoreCase("VALUES"))
            throw new IllegalArgumentException("Sintaxis no válida");

        String values = parts[5];

        String[] colBrk = columns.split(",");
        String[] valBrk = values.split(",");

        if (colBrk.length != valBrk.length)
            throw new RuntimeException("Los valores a insertar y las columnas no coinciden");

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

        for (int i = 0; i < colBrk.length; i++)
            colBrk[i] = colBrk[i].trim();

        for (int i = 0; i < colBrk.length; i++) {
            boolean flag = false;
            for (int j = 0; j < types.size(); j++) {
                if (colBrk[i].equals(types.get(j).getName())) {
                    flag = true;
                    break;
                }
            }

            if (!flag)
                throw new IllegalArgumentException("Valores de insert into inválidos");

        }

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

        try (BufferedWriter bf = new BufferedWriter(
                new FileWriter(FileManagement.getDatabasePath() + name + ".csv", true))) {
            bf.write(stringToWrite);
            bf.newLine();
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        return "Registro éxitoso";
    }

    public static boolean verifyUniqueness(String colName, String tableName, String value) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
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
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                return true;
            case "float":
                try {
                    Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                if (T.getLength() != -1 && value.length() - 1 - value.indexOf(".") > T.getLength())
                    throw new NumberFormatException("Precisión equivocada");
                return true;

            case "double":
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Tipo de dato incorrecto");
                }
                if (T.getLength() != -1 && value.contains(".")
                        && value.length() - 1 - value.indexOf(".") > T.getLength())
                    throw new NumberFormatException("Precisión equivocada");
                return true;
            case "varchar":
                if (value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'')
                    throw new NumberFormatException("Tipo de dato incorrecto");
                if (T.getLength() != -1 && value.length() > T.getLength())
                    throw new Exception("Mayor longitud");
                return true;
            case "date":
                if (value.length() != 10)
                    throw new NumberFormatException("Formato de fecha incorrecto");
                if (value.charAt(2) != '/' || value.charAt(5) != '/')
                    throw new NumberFormatException("Formato de fecha incorrecto");

                for (int i = 0; i < value.length(); i++) {
                    if (i == 2 || i == 5)
                        continue;
                    if (!Character.isDigit(value.charAt(i)))
                        throw new NumberFormatException("Formato de fecha incorrecto");
                }
                return true;
        }

        return false;
    }

    public static String select(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new FileNotFoundException("No se ha accedido a ninguna base de datos");

        // HashMap que contiene los alias
        HashMap<String, String> alias = new HashMap<>();

        // Args son todos los argumentos del select, condicionales guarda los
        // condicionales
        String args = "", condicionales = "", tableName = "";
        String[] words = query.split(" ");
        int index = 0;

        for (int i = 1; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("FROM")) {
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

            if (linea.length == 3)
                if (linea[2].charAt(0) == '\'' && linea[2].charAt(linea[2].length() - 1) == '\'') {
                    alias.put(linea[0], linea[2]);
                    header += linea[0] + ",";
                } else
                    throw new Exception("Alias incorrectos");
            else if (linea.length == 1)
                header += linea[0] + ",";
            else
                throw new Exception("Sintaxis incorrecta");
        }
        header = header.substring(0, header.length() - 1);

        if (index + 1 < words.length)
            tableName = words[++index];
        else
            throw new Exception("Sintaxis incorrecta");

        if (!FileManagement.searchForTable(tableName))
            throw new IOException("Tabla no encontrada");

        /*  */

        try {
            for (int i = index + 1; i < words.length; i++) {
                if (Utilities.isReservedWord(words[i]))
                    continue;
                condicionales += words[i] + " ";
            }
        } catch (Exception e) {
            throw new Exception("Sintaxis incorrecta");
        }

        // Header guarda los argumentos sin el alias
        // System.out.println(header);

        if (!header.equals("*")) {
            ArrayList<TypeBuilder> tp = FileManagement.decompressInfo(tableName);
            String[] hdbrk = header.split(",");

            for (int i = 0; i < hdbrk.length; i++) {
                boolean flag = false;
                for (int j = 0; j < tp.size(); j++) {
                    if (tp.get(j).getName().equals(hdbrk[i])) {
                        flag = true;
                        break;
                    }
                }
                if (!flag)
                    throw new IllegalArgumentException("Parámetros desconocidos en el select");
            }
        }

        String tableHeader = Utilities.getHeaderOfTable(tableName);

        HashSet<Integer> indexes = new HashSet<>();
        if (header.equals("*")) {
            String[] tableHeaderBreak = tableHeader.split(",");
            for (int i = 0; i < tableHeaderBreak.length; i++)
                indexes.add(i);
        } else {
            String[] tableHeaderBreak = tableHeader.split(",");
            for (int i = 0; i < tableHeaderBreak.length; i++)
                for (int j = 0; j < header.split(",").length; j++)
                    if (tableHeaderBreak[i].equals(header.split(",")[j])) {
                        indexes.add(i);
                        break;
                    }
        }

        // for(String key : indexMap.keySet())
        // System.out.println(key + " " + indexMap.get(key));

        // for(String key : typeMap.keySet())
        // System.out.println(key + " " + typeMap.get(key));

        // Condicionales guarda el string de los condicionales
        // System.out.println(condicionales);

        // Alias guarda los alias
        // for(String key : alias.keySet())
        // System.out.println(key + " " + alias.get(key));

        ArrayList<String> resultTable = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            String line;
            resultTable.add(line = br.readLine());
            while ((line = br.readLine()) != null) {
                if (Where.manageWhere(condicionales, line, tableName))
                    resultTable.add(line);
            }
        } catch (Exception e) {
            throw new FileNotFoundException("No se encontró el archivo");
        }

        String[] headerBreak = resultTable.get(0).split(",");

        for (int i = 0; i < headerBreak.length; i++)
            if (indexes.contains(i))
                if (alias.containsKey(headerBreak[i]))
                    System.out.print(alias.get(headerBreak[i]) + " ");
                else
                    System.out.print(headerBreak[i] + " ");
        System.out.println();
        for (int i = 1; i < resultTable.size(); i++) {
            String[] lineBrk = resultTable.get(i).split(",");
            for (int j = 0; j < lineBrk.length; j++)
                if (indexes.contains(j))
                    System.out.print(lineBrk[j] + " ");
            System.out.println();
        }


        return "Select realizado con éxito";
    }

    public static void manageWhere(String condiciones, String tableName) throws Exception {
        String path = FileManagement.getDatabasePath() + tableName + ".csv";

        String temp = "";
        for (int i = 0; i < condiciones.length(); i++) {
            if (condiciones.charAt(i) == ' ')
                continue;
            temp += condiciones.charAt(i);
        }
        condiciones = temp;

        condiciones = condiciones.replace(">=", "tempOne");
        condiciones = condiciones.replace("<=", "tempTwo");
        condiciones = condiciones.replace("'", "\"");
        condiciones = condiciones.replace("=", "==");
        condiciones = condiciones.replace("<>", "!=");

        // Regex para cambiar == a .equalsTo en strings
        String regexOne = "==\\\"([^\\\"]*)\\\"";
        Pattern pattern = Pattern.compile(regexOne);
        Matcher matcher = pattern.matcher(condiciones);
        condiciones = matcher.replaceAll(".equals(\"\'$1\'\")");

        condiciones = condiciones.replace("AND", "&&");
        condiciones = condiciones.replace("and", "&&");
        condiciones = condiciones.replace("OR", "||");
        condiciones = condiciones.replace("or", "||");
        condiciones = condiciones.replace("tempTwo", "<=");
        condiciones = condiciones.replace("tempOne", ">=");

        String header = "";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            header = br.readLine();
            ArrayList<TypeBuilder> tp = FileManagement.decompressInfo(tableName);

            for (int i = 0; i < condiciones.length(); i++) {
                for (int j = 0; j < tp.size(); j++) {
                    if (condiciones.contains(tp.get(j).getName())) {
                        TypeBuilder t = tp.get(j);
                        switch (tp.get(j).getDataType().toLowerCase()) {
                            case "int":
                                condiciones = condiciones.replace(t.getName(),
                                        "Integer.parseInt(String.valueOf(arrBrk[" + j + "]))");
                                break;
                            case "float":
                                condiciones = condiciones.replace(t.getName(),
                                        "(Math.round(Float.parseFloat(arrBrk[" + j + "]) * 10000000.0) / 10000000.0)");
                                break;
                            case "double":
                                condiciones = condiciones.replace(t.getName(),
                                        "(Math.round(Double.parseDouble(String.valueOf(arrBrk[" + j + "]))");
                                break;
                            case "varchar":
                                condiciones = condiciones.replace(t.getName(), "String.valueOf(arrBrk[" + j + "])");
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }
        // Hasta acá está full trabajada la condición
        // Integer.parseInt(String.valueOf(arrBrk[0]))==10&&String.valueOf(arrBrk[1]).equals("miguel")
        // System.out.println(condiciones);

        // Path para mandarle la sentencia y el nombre de la tabla
        String temporalAuxInfoPath = (new File("")).getAbsolutePath() + "/temporalAuxInfo.txt";
        String temporalTableTemp = new File("").getAbsolutePath()
                + "/dataBaseManager/src/main/java/edu/upvictoria/fpoo/TablaTemp.java";
        String tableDir = FileManagement.getDatabasePath() + tableName + ".csv";

        try (BufferedWriter bf = new BufferedWriter(new FileWriter(temporalAuxInfoPath))) {
            bf.write(tableDir);
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temporalTableTemp))) {
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
                    "        String temporalAuxInfoPath = (new File(\"\")).getAbsolutePath() + \"/temporalAuxInfo.txt\";\n"
                    + //
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
                    "                if(" + condiciones + ")\n" + //
                    "                tabla.add(line);\n" + //
                    "            }\n" + //
                    "        } catch (Exception e){\n" + //
                    "            System.out.println(e.getMessage());\n" + //
                    "        }\n" + //
                    "\n" + //
                    "        try(BufferedWriter bw = new BufferedWriter(new FileWriter((new File(\"\")).getAbsolutePath() + \"/temporalAuxInfo.csv\"))){\n"
                    + //
                    "            for(String row : tabla){\n" + //
                    "                bw.write(row);\n" + //
                    "                bw.newLine();\n" + //
                    "            }\n" + //
                    "        } catch (Exception e){\n" + //
                    "            System.out.println(e.getMessage());\n" + //
                    "        }\n" + //
                    "    }\n" + //
                    "}");
        } catch (IOException e) {
            throw new Exception("No se pudo crear el archivo");
        }

        try {
            Process processOne = Runtime.getRuntime().exec("javac " + temporalTableTemp);
            processOne.waitFor(2000, TimeUnit.SECONDS);
            Process processTwo = Runtime.getRuntime()
                    .exec("java -classpath dataBaseManager/src/main/java/ edu.upvictoria.fpoo.TablaTemp");
            processTwo.waitFor(2000, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new Exception("No se encontró el archivo");
        }
    }

    public static String deleteFrom(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new FileNotFoundException("No se ha accedido a ninguna base de datos");

        String[] words = query.split(" ");

        if (words.length < 3)
            throw new Exception("Sintaxis incorrecta");

        String tableName = words[2];

        if (!FileManagement.searchForTable(tableName))
            throw new Exception("No se ha encontrado el archivo;");

        String arguments = "";

        boolean flag = false;
        String condicionales = "";
        if (query.contains("WHERE") || query.contains("where")) {

            for (int i = 3; i < words.length; i++) {
                if (Utilities.isReservedWord(words[i]))
                    continue;
                condicionales += words[i];
            }
            System.out.println(condicionales);
            flag = true;
        } else if (words.length > 3)
            throw new IllegalArgumentException("Sintaxis no válida");

        if (!flag) {
            String path = FileManagement.getDatabasePath() + tableName;
            String header = "";

            try (BufferedReader bf = new BufferedReader(new FileReader(path + ".csv"))) {
                header = bf.readLine();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("No se encontró el archivo");
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path + ".csv"))) {
                bw.write(header);
                bw.newLine();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("No se encontró el archivo");
            }
            return "Update realizado con éxito";
        }

        String path = (new File("").getAbsolutePath()) + "/temporalAuxInfo.csv";

        ArrayList<TypeBuilder> tp = FileManagement.decompressInfo(tableName);

        int indexPK = 0;
        for (int i = 0; i < tp.size(); i++)
            if (tp.get(i).isPrimaryKey()) {
                indexPK = i;
                break;
            }
        manageWhere(condicionales, tableName);

        if (!new File(path).exists()) {
            Utilities.deleteFilesFromWhere();
            throw new IllegalArgumentException("Where invalido");
        }

        HashSet<String> idsAExcluir = new HashSet<String>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineBrk = line.split(",");
                idsAExcluir.add(lineBrk[indexPK]);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Parámetros del delete incorrectos");
        }

        ArrayList<String> nuevaTabla = new ArrayList<>();

        String pathTableName = FileManagement.getDatabasePath() + tableName + ".csv";
        String header;
        try (BufferedReader bf = new BufferedReader(new FileReader(pathTableName))) {
            header = bf.readLine();
            String line;
            while ((line = bf.readLine()) != null) {
                String[] lineBrk = line.split(",");
                if (!idsAExcluir.contains(lineBrk[indexPK]))
                    nuevaTabla.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("No se encontró el archivo");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(pathTableName))) {
            bw.write(header);
            bw.newLine();

            for (int j = 0; j < nuevaTabla.size(); j++) {
                bw.write(nuevaTabla.get(j));
                bw.newLine();
            }
        }

        Utilities.deleteFilesFromWhere();
        return "Update realizado con éxito";
    }

    public static String update(String query) throws Exception {
        // update table set val1=v1... where ...
        if (FileManagement.getDatabasePath() == null)
            throw new NullPointerException("No hay ninguna base de datos a la que hayas accedido");

        String[] words = query.split(" ");

        if (words.length < 3)
            throw new IllegalArgumentException("No hay suficientes parámetros");

        String tableName = words[1];
        if (!words[2].equalsIgnoreCase("SET"))
            throw new IllegalArgumentException("No hay suficientes parámetros");

        if (!FileManagement.searchForTable(tableName))
            throw new FileNotFoundException("No encontré el archivo");

        String set = "";
        System.out.println(tableName);

        int index = 2;
        for (int i = index; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("WHERE"))
                break;
            if (Utilities.isReservedWord(words[i]))
                continue;
            set += words[i];
        }

        if (set.equals(""))
            throw new IllegalArgumentException("Set inválido");

        String[] setBrk = set.split(",");

        for (int i = 0; i < setBrk.length; i++)
            setBrk[i] = setBrk[i].trim();

        HashMap<String, String> values = new HashMap<>();

        for (int i = 0; i < setBrk.length; i++) {
            String[] line = setBrk[i].split("=");

            if (line.length > 2)
                throw new IllegalArgumentException("Set inválido,");

            if (!values.containsKey(line[0]))
                values.put(line[0], line[1]);
            else
                throw new IllegalArgumentException("Set inválido");
        }

        String header = "";

        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            header = br.readLine();
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        for (String key : values.keySet()) {
            boolean flag = false;
            for (String h : header.split(",")) {
                if (h.equals(key)) {
                    flag = true;
                    break;
                }
            }
            if (!flag)
                throw new IllegalArgumentException("Set inválido");
        }

        ArrayList<TypeBuilder> tp = FileManagement.decompressInfo(tableName);
        boolean hasPK = false;

        for (String key : values.keySet()) {
            for (TypeBuilder t : tp) {
                if (t.getName().equals(key) && t.isPrimaryKey()) {
                    hasPK = true;
                    break;
                }
            }
        }

        for (String key : values.keySet()) {
            for (TypeBuilder t : tp) {
                if (t.getName().equals(key)) {
                    checkType(t, values.get(key));
                }
            }
        }

        boolean flag = false;

        if (query.contentEquals("WHERE") || query.contains("where"))
            flag = true;

        if (!flag) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add(header);

            try (BufferedReader br = new BufferedReader(
                    new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] lineBrk = line.split(",");
                    for (String key : values.keySet()) {
                        for (int i = 0; i < lineBrk.length; i++) {
                            if (header.split(",")[i].equals(key)) {
                                lineBrk[i] = values.get(key);
                                break;
                            }
                        }
                    }
                    String newLine = "";
                    for (int i = 0; i < lineBrk.length; i++)
                        newLine += lineBrk[i] + ",";
                    newLine = newLine.substring(0, newLine.length() - 1);
                    lines.add(newLine);
                }
            } catch (IOException e) {
                throw new IOException("No se pudo abrir el archivo");
            }

            if (lines.size() == 1)
                throw new IllegalArgumentException("No se encontraron registros");

            ArrayList<TypeBuilder> types = FileManagement.decompressInfo(tableName);

            int indexPK = 0;
            for (int i = 0; i < types.size(); i++)
                if (types.get(i).isPrimaryKey()) {
                    indexPK = i;
                    break;
                }

            HashSet<String> ids = new HashSet<>();

            for (int i = 1; i < lines.size(); i++) {
                String[] lineBrk = lines.get(i).split(",");
                if (ids.contains(lineBrk[indexPK]))
                    throw new IllegalArgumentException("PK repetida");
                ids.add(lineBrk[indexPK]);
            }

            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(FileManagement.getDatabasePath() + tableName + ".csv"))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            } catch (IOException e) {
                throw new IOException("No se pudo abrir el archivo");
            }
            return "Update realizado con éxito";
        }

        String condicionales = "";

        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("WHERE") && i + 1 < words.length) {
                for (int j = i + 1; j < words.length; j++) {
                    if (Utilities.isReservedWord(words[j]))
                        continue;
                    condicionales += words[j];
                }
            }
        }

        manageWhere(condicionales, tableName);

        if (!new File((new File("").getAbsolutePath()) + "/temporalAuxInfo.csv").exists()) {
            Utilities.deleteFilesFromWhere();
            throw new IllegalArgumentException("Where inválido");
        }

        HashSet<String> ids = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new FileReader((new File("").getAbsolutePath()) + "/temporalAuxInfo.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineBrk = line.split(",");
                ids.add(lineBrk[0]);
            }
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            // i need to save all the lines, the not modified and modified ones in lines
            // because then we are going to verify the pk's

            String line = br.readLine();
            lines.add(line);

            while ((line = br.readLine()) != null) {
                String[] lineBrk = line.split(",");
                if (ids.contains(lineBrk[0])) {
                    for (String key : values.keySet()) {
                        for (int i = 0; i < lineBrk.length; i++) {
                            if (header.split(",")[i].equals(key)) {
                                lineBrk[i] = values.get(key);
                                break;
                            }
                        }
                    }
                }
                String newLine = "";
                for (int i = 0; i < lineBrk.length; i++)
                    newLine += lineBrk[i] + ",";
                newLine = newLine.substring(0, newLine.length() - 1);
                lines.add(newLine);
            }

        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        ArrayList<TypeBuilder> types = FileManagement.decompressInfo(tableName);

        int indexPK = 0;
        for (int i = 0; i < types.size(); i++)
            if (types.get(i).isPrimaryKey()) {
                indexPK = i;
                break;
            }

        HashSet<String> idsAExcluir = new HashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String[] lineBrk = lines.get(i).split(",");
            if (idsAExcluir.contains(lineBrk[indexPK])) {
                Utilities.deleteFilesFromWhere();
                throw new IllegalArgumentException(
                        "ERROR: Se va a duplicar más de una primary key, lo que arriesga la integridad de los datos");
            }
            idsAExcluir.add(lineBrk[indexPK]);
        }

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        Utilities.deleteFilesFromWhere();
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
                    if (parenthesis < 0)
                        return false;
                    break;
            }
        }

        return parenthesis == 0;
    }
}