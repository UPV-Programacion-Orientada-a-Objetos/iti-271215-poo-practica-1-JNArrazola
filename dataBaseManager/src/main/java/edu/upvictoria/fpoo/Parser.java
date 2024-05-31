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
        FileManagement.initialValidations();
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
            Utilities.handleException(e);
            throw new Exception(e.getMessage());
        } catch (Error e) {
            Utilities.handleError(e);
            throw new Error(e.getMessage());
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
                for (int i = 0; i < individualArgumentBreak.length; i++)
                    individualArgumentBreak[i] = individualArgumentBreak[i].trim();

                for (int i = 1; i < individualArgumentBreak.length; i++)
                    if (!Utilities.isValidReservedWordCreateTable(individualArgumentBreak[i]))
                        throw new IllegalArgumentException(
                                individualArgumentBreak[i] + " no es una palabra reservada válida");

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
                if (type.contains("(")){
                    if(type.contains(")"))
                        length = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
                    else
                        throw new IllegalArgumentException("Falta cerrar el paréntesis");
                    if(length.equals(""))
                        throw new IllegalArgumentException("Longitud inválida");
                }
                if (!(length.equalsIgnoreCase(""))
                        && (type.toLowerCase().contains("int") || type.toLowerCase().contains("date")))
                    throw new IllegalArgumentException("INT y DATE no pueden tener precisión");

                // try to parse length
                if (!length.equals(""))
                    try {
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
                        canBeNull = false;
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

        String name = "";
        try {
            name = parts[2];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Faltó el nombre de la tabla");
        }

        if (!FileManagement.searchForTable(name))
            throw new FileNotFoundException("No se encontró el archivo");

        String columns = "";
        try {
            columns = parts[3];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Faltaron columnas en el insert into");
        }

        if (columns.equalsIgnoreCase("VALUES"))
            throw new IllegalArgumentException("Falta especificar columnas");

        try {
            if (!parts[4].equalsIgnoreCase("VALUES"))
                throw new IllegalArgumentException("Sintaxis no válida");
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Faltaron valores en el insert into");
        }

        String values = "";

        try {
            values = parts[5];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Faltaron valores en el insert into");
        }

        if (values.isEmpty())
            throw new IllegalArgumentException("No se encontraron valores en el insert into");

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
                    throw new Exception("Alias incorrectos: " + linea[2]);
            else if (linea.length == 1)
                header += linea[0] + ",";
            else
                throw new Exception("Sintaxis incorrecta: " + argsTrabajados[i]);
        }
        header = header.substring(0, header.length() - 1);

        if (index + 1 < words.length)
            tableName = words[++index];
        else
            throw new Exception("Sintaxis incorrecta");

        if (!FileManagement.searchForTable(tableName))
            throw new IOException("Tabla no encontrada: " + tableName);

        try {
            for (int i = index + 1; i < words.length; i++) {
                if (Utilities.isReservedWord(words[i])&& !words[i].equalsIgnoreCase("Null"))
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
                    throw new IllegalArgumentException("Parámetros desconocidos en el select: " + hdbrk[i]);
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

        System.out.println(condicionales);
        ArrayList<String> resultTable = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            String line;
            resultTable.add(line = br.readLine());
            while ((line = br.readLine()) != null) {
                if (Where.manageWhere(condicionales, line, tableName))
                    resultTable.add(line);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
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

    public static String deleteFrom(String query) throws Exception {
        if (FileManagement.getDatabasePath() == null)
            throw new FileNotFoundException("No se ha accedido a ninguna base de datos");

        String[] words = query.split(" ");

        if (words.length < 3)
            throw new Exception("Sintaxis incorrecta");

        String tableName = words[2];

        if (!FileManagement.searchForTable(tableName))
            throw new Exception("No se ha encontrado el archivo;");

        String condicionales = "";
        if (query.contains("WHERE") || query.contains("where")) {
            for (int i = 3; i < words.length; i++) {
                if (Utilities.isReservedWord(words[i]))
                    continue;
                condicionales += words[i] + " ";
            }
        }

        System.out.println(condicionales);
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            String line;
            lines.add(line = br.readLine());
            while ((line = br.readLine()) != null) {
                if (!Where.manageWhere(condicionales, line, tableName))
                    lines.add(line);
            }
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error de sintaxis");
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

        return "Delete realizado con éxito";
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

        ArrayList<TypeBuilder> tb = FileManagement.decompressInfo(tableName);
        // i need to check types
        for (String key : values.keySet()) {
            for (TypeBuilder type : tb) {
                if (type.getName().equals(key)) {
                    checkType(type, values.get(key));
                    break;
                }
            }
        }

        String condicionales = "";
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("WHERE") && i + 1 < words.length) {
                for (int j = i + 1; j < words.length; j++) {
                    if (Utilities.isReservedWord(words[j]))
                        continue;
                    condicionales += words[j] + " ";
                }
            }
        }
        condicionales = condicionales.trim();

        ArrayList<String> lines = new ArrayList<>();
        lines.add(header);

        try (BufferedReader br = new BufferedReader(
                new FileReader(FileManagement.getDatabasePath() + tableName + ".csv"))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (Where.manageWhere(condicionales, line, tableName)) {
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
                } else
                    lines.add(line);
            }
        } catch (IOException e) {
            throw new IOException("No se pudo abrir el archivo");
        }

        int indPrimaryKey = 0;
        for (int i = 0; i < tb.size(); i++) 
            if(tb.get(i).isPrimaryKey()){
                indPrimaryKey = i;
                break;
            }
        

        HashSet<String> ids = new HashSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] lineBrk = lines.get(i).split(",");
            if (ids.contains(lineBrk[indPrimaryKey]))
                throw new IllegalArgumentException(
                        "Esta sentencia UPDATE asignaría dos ID's iguales, lo cual no es permitido");
            ids.add(lineBrk[indPrimaryKey]);
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