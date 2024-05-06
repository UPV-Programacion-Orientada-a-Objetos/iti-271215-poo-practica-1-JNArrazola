package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Actividad 1: Biblioteca de sentencias SQL
 * Asignatura: Programación Orientada a Objetos
 * Docente: Dr. Said Polanco Martagón
 * ---------------------------------------------
 * Alumno: Joshua Nathaniel Arrazola Elizondo
 * Matrícula: 2230023
 * */
public class App {
    private final BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        // Initial validations for the entire work of the program
        FileManagement.initialValidations();

        // App launcher
        App app = new App();
        app.run();
    }

    /**
     * Main body of app, user is gonna insert queries and changes are gonna be showed1
     * @author Joshua Arrazola
     * */
    public void run(){
        boolean runFlag = true;

        // Here the user is gonna insert the queries
        String query = "";
        do {
            if(query.isEmpty())
                System.out.println("Ingresa la query (ingresa 'x' si ya no hay mas ENTER): ");

            String creatingQuery = Utilities.readQuery(bf).trim();

            if (creatingQuery.equals("X")){
                Parser.parseQuery(query);
                query = "";
            }
            else {
                if (!query.isEmpty()) {
                    query += " ";
                }
                query += creatingQuery;
            }
        }while (runFlag);
    }

}
