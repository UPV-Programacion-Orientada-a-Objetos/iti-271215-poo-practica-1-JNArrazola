package edu.upvictoria.fpoo;

import org.junit.Test;
import junit.framework.TestCase;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for simple App.
 */
public class AppTest

extends TestCase {
    public AppTest(String testName) {
        super(testName);
    }

    private final String testTable = new File("").getAbsolutePath() + "/test/";
    private final String tableName = "testTable";

    public void testApp() {
        assertTrue(true);
    }

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
        String[] query = {"masmcamd mwd cmqmd", "esto no es una sentencia", "esto tampoco",
                "tengo hambre", "sexto cuatrimestr"};

        for (String s : query)
            assertEquals("No se reconoció la sentencia", Parser.parseQuery(s));
    }

    /**
     * Use database with a totally new and not existing directory
     */
    @Test
    public void testUseDatabase_valid() throws Exception {
        FileManagement.initialValidations();
        String query = "USE " + (new File("").getAbsolutePath()) + "/testCase";

        String result = Parser.parseQuery(query);

        File expectedFile = new File((new File("").getAbsolutePath()) + "/testCase");
        assertTrue(expectedFile.exists());
        assertEquals("Directorio creado con éxito", result);
        expectedFile.delete();
    }


    /**
     * Invalid path test
     */
    @Test
    public void testUseDatabase_invalid() throws Exception {
        FileManagement.initialValidations();
        String query = "USE /W;";

        Exception generatedException = assertThrows(Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("Error al crear el directorio", generatedException.getMessage());
    }


    /**
     * Permission problem
     */
    @Test
    public void testUseDatabase_invalid2() throws Exception {
        FileManagement.initialValidations();
        String query = "USE /;";

        Exception generatedException = assertThrows(java.lang.Exception.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals("Error al crear el directorio", generatedException.getMessage());
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
     * */
    @Test
    public void testParenthesis() {
        FileManagement.initialValidations();
        FileManagement.setDatabasePath(new File("").getAbsolutePath() + "/");
        String[] strs = {"SELECT * FROM (Locations, CREATE DATABASE(", "((())", "()()()("};

        for (String s : strs)
            assertFalse(Parser.parenthesisCheck(s));
    }

    


}
