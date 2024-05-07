package edu.upvictoria.fpoo;

import org.junit.Test;
import junit.framework.TestCase;
import java.io.File;
import java.nio.file.FileSystemException;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
   /* public static Test suite()
    {
        return (Test) new TestSuite( AppTest.class );
    }*/

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    /**
     * Test to validate if some given word is a reserved word
     * */
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
     * */
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
     * */
    @Test
    public void tesHasValidChars_NotValid(){
        assertFalse(Utilities.hasValidChars("/home/path"));
        assertFalse(Utilities.hasValidChars("||name||"));
        assertFalse(Utilities.hasValidChars(".exec"));
    }

    /**
     * Not valid queries
     * */
    @Test
    public void testParse() throws Exception {
        String[] query = {"masmcamd mwd cmqmd", "esto no es una sentencia", "esto tampoco",
        "tengo hambre", "sexto cuatrimestr"};

        for(String s : query)
            assertEquals("No se reconoció la sentencia", Parser.parseQuery(s));
    }

    /**
     * Use database with a totally new and not existing directory
     * */
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
     * */
    @Test
    public void testUseDatabase_invalid() throws Exception {
        FileManagement.initialValidations();
        String query = "USE /W";

        Exception generatedException = assertThrows(FileSystemException.class, () -> {
            Parser.parseQuery(query);
        });

        assertEquals(generatedException.getMessage(), "Error al crear el directorio");
    }
}
