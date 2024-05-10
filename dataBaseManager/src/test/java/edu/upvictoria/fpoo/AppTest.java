package edu.upvictoria.fpoo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest

        extends TestCase {
    public AppTest(String testName) {
        super(testName);
    }

    private final static String testTable = new File("").getAbsolutePath() + "/test/";
    private final static String tableName = "testTable";
    private final static String fullPath = testTable + tableName + ".csv";

    private static void resetTable(){
        ArrayList<String> data = new ArrayList<>();
        data.add("id,name,money,height");
        data.add("1,'pedro',1000,1.60");
        data.add("2,'martin',2000,1.80");
        data.add("3,'joshua',1500,1.60");
        data.add("4,'said',4000,1.75");
        data.add("5,'uriegas',3500,1.50");  
        data.add("6,'joshua',1500,1.60");
        data.add("7,'said',4000,1.75");
        data.add("8,'uriegas',3500,1.50");
        data.add("9,'joshua',1500,1.60");
        data.add("10,'said',4000,1.75");
        data.add("11,'uriegas',3500,1.50");
        data.add("12,'joshua',1500,1.60");

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fullPath), false))){
            for(String s : data){
                bw.write(s);
                bw.newLine();
            }
            bw.newLine();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void testApp() {
        assertTrue(true);
    }

    /**
     * Test to validate if a word is reserved
     */
    @Test
    public void testIsReservedWord_True() {
        FileManagement.initialValidations();

        assertTrue(Utilities.isReservedWord("SELECT"));
        assertTrue(Utilities.isReservedWord("INSERT"));
        assertTrue(Utilities.isReservedWord("UPDATE"));
        assertTrue(Utilities.isReservedWord("FROM"));
        assertTrue(Utilities.isReservedWord("INT"));
        assertTrue(Utilities.isReservedWord("VARCHAR"));
    }

    /**
     * Test to validate if some given word
     */
    @Test
    public void testHasValidChars_AllValid() {
        // Todas las letras son válidas
        assertTrue(Utilities.hasValidChars("abcABC123"));
        assertTrue(Utilities.hasValidChars("SELECT"));
        assertTrue(Utilities.hasValidChars("INSERT"));
        assertTrue(Utilities.hasValidChars("EMPLOYEES"));
    }

    /**
     *
     */
    @Test
    public void tesHasValidChars_NotValid() {
        assertFalse(Utilities.hasValidChars("/home/path"));
        assertFalse(Utilities.hasValidChars("||name||"));
        assertFalse(Utilities.hasValidChars(".exec"));
    }

    /**
     * Not valid queries
     */
    @Test
    public void testParse() throws Exception {
        String[] query = { "masmcamd mwd cmqmd", "esto no es una sentencia", "esto tampoco",
                "tengo hambre", "sexto cuatrimestr" };

        for (String s : query)
            assertEquals("No se reconoció la sentencia", Parser.parseQuery(s));
    }

    /**
     * Use database with a totally new and not existing directory
     */

    /**
     * Invalid path test
     */
    @Test
    public void testUseDatabase_invalid() throws Exception {
        FileManagement.initialValidations();
        String query = "USE /W";

        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("El directorio no existe", generatedException.getMessage());
    }

    /**
     * Permission problem
     */
    @Test
    public void testUseDatabase_invalid2() throws Exception {
        FileManagement.initialValidations();
        String query = "USE /";

        Exception generatedException = assertThrows(java.lang.Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("No tengo permisos", generatedException.getMessage());
    }

    /**
     * Crear tabla
     * Sin path para guardar el archivo previamente configurado
     */
    @Test
    public void testCreateTable() throws Exception {
        FileManagement.initialValidations();
        FileManagement.setDatabasePath(null);
        String query = "CREATE TABLE EMP (ID int not null);";

        Exception generatedException = assertThrows(java.lang.Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("No hay path asignado", generatedException.getMessage());
    }

    /*
     * Checar que los paréntesis estén correctos
     */
    @Test
    public void testParenthesis() {
        FileManagement.initialValidations();
        FileManagement.setDatabasePath(new File("").getAbsolutePath() + "/");
        String[] strs = { "SELECT * FROM (Locations, CREATE DATABASE(", "((())", "()()()(" };

        for (String s : strs)
            assertFalse(Parser.parenthesisCheck(s));
    }

    /**
     * Create a valid table
     * 
     * @throws Exception
     */
    @Test
    public void testCreateTable_valid() throws Exception {
        FileManagement.initialValidations();
        String path = new File("").getAbsolutePath() + "/";
        FileManagement.setDatabasePath(path);
        String query = "CREATE TABLE JOSHUA(ID int not null primary key, NAME varchar(50) not null, MONEY int not null, HEIGHT float not null)";

        String result = Parser.parseQuery(query);

        File expectedFile = new File(path + "JOSHUA.csv");
        File auxFile = new File(path + "JOSHUA_aux.txt");

        assertTrue(expectedFile.exists());
        assertEquals("Tabla creada correctamente", result);
        expectedFile.delete();
        auxFile.delete();
    }

    /**
     * Without primary key
     * @throws Exception
     */
    @Test
    public void testCreateTable_notValidPrimaryKey() throws Exception {
        FileManagement.initialValidations();
        String path = new File("").getAbsolutePath() + "/";
        FileManagement.setDatabasePath(path);
        String query = "CREATE TABLE JOSHUA (ID int not null, NAME varchar(50) not null, MONEY int not null, HEIGHT float not null)";


        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("No se puede crear una tabla sin primary key definida", generatedException.getMessage());
    }

    /**
     * Double primary key
     * @throws Exception
      */
    @Test
    public void testDoublePrimaryKey() throws Exception {
        FileManagement.initialValidations();
        String path = new File("").getAbsolutePath() + "/";
        FileManagement.setDatabasePath(path);
        String query = "CREATE TABLE EMPLEADOS(ID INT NOT NULL PRIMARY KEY, NAME VARCHAR(50) NOT NULL PRIMARY KEY)";

        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("No puede haber dos primary keys", generatedException.getMessage());
    }

    /**
     * Here we are passing some words that nothing have to do with the query
     * @throws Exception
      */
    @Test
    public void testCreateTableInvalidArguments() throws Exception {
        FileManagement.initialValidations();
        String path = new File("").getAbsolutePath() + "/";
        FileManagement.setDatabasePath(path);
        String query = "create table a(id int not null primary key pedro elizondo);";

        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("Argumentos incorrectos", generatedException.getMessage());
        new File(new File("").getAbsolutePath() + "/JOSHUA.csv").delete();
        new File(new File("").getAbsolutePath() + "/JOSHUA_aux.txt").delete();
    }


    // ---------------------------------------------- SELECT TEST ----------------------------------------------
    /**
     * Select with an invalid alias
      */
    @Test
    public void testAlias(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("select name as 'nombre', id as apellido from testTables;");
        });
        assertEquals("Alias incorrectos", e.getMessage());
    }

    /**
     * Select with invalid arguments
      */
    @Test
    public void testArguments(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("select id, a from testTable");
        });
        assertEquals("Parámetros desconocidos en el select", e.getMessage());
    }

    @Test 
    public void testTableDoesntExist(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("select * from a");
        });
        assertEquals("Tabla no encontrada", e.getMessage());
    }

    // Select tests
    /**
     * Path is null
     */
    @Test
    public void testSelect_nullPath() throws Exception {
        FileManagement.initialValidations();
        FileManagement.setDatabasePath(null);
        String query = "SELECT * FROM JOSHUA";

        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("No se ha accedido a ninguna base de datos", generatedException.getMessage());
    }

    /**
     * Table does not exist
     */
    @Test
    public void testSelect_tableDoesNotExist() throws Exception {
        FileManagement.initialValidations();
        String path = new File("").getAbsolutePath() + "/";
        FileManagement.setDatabasePath(path);
        String query = "SELECT * FROM JOSHUA";

        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("Tabla no encontrada", generatedException.getMessage());
    }

    // ---------------------------------------------- DELETE TEST ----------------------------------------------
    /**
     * File does not exist case
      */
    @Test
    public void testDeleteTableDoesNotExist() throws Exception {
        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test");
            Parser.parseQuery("DELETE FROM tesamd WHERE id = 1;");
        });

        assertEquals("No se ha encontrado el archivo;", generatedException.getMessage());
    }

    //---------------------------------------------- CREATE TEST ----------------------------------------------
    /**
     * Create test
      */
    @Test
    public static void testCreateTableWithDuplicateKey(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("CREATE TABLE PRUEBA(ID INT NOT NULL PRIMARY KEY, ID INT NOT NULL PRIMARY KEY)");
        });
        assertEquals("No puede haber dos primary keys", e.getMessage());
    }

    

    // ---------------------------------------------- UPDATE TEST ----------------------------------------------
    /**
     * Update with invalid arguments
      */
    @Test
    public static void testUpdateInvalid(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("update testTable set a=2, b=3 where i = 1");
        });

        assertEquals("Set inválido", e.getMessage());
    }
    
    // ---------------------------------------------- INSERT TEST ----------------------------------------------
    /**
     * Insert with columns not matching the values
      */
    @Test
    public static void testInsertInvalid(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("insert into testTable (id, name) values (1, 'joshua', 2)");
        });

        assertEquals("Los valores a insertar y las columnas no coinciden", e.getMessage());
    }

    /**
     * Repeated primary key
      */
    @Test
    public static void testInsertRepeatedPrimaryKey(){
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("insert into testTable (id, name) values (1,'jose')");
        });

        assertEquals("PK repetida", e.getMessage());
    }

    /**
     * Insert with invalid types of arguments
      */
    @Test
    public static void testInsertInvalidTypes(){
        resetTable();
        Exception e = assertThrows(Exception.class, () -> {
            Parser.parseQuery("use /home/jarrazola/Documents/iti-271215-poo-practica-1-JNArrazola/test/");
            Parser.parseQuery("INSERT INTO testTable (id,name) values (15,34)");
            Parser.parseQuery("DELETE FROM testTable WHERE values id = 15");
        });

        assertEquals("Tipo de dato incorrecto", e.getMessage());
        resetTable();
    }
}

