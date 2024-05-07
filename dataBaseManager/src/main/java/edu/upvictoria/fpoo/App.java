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

    public static void main(String[] args) {
        // Initial validations for the entire work of the program
        FileManagement.initialValidations();

        // App launcher
        App app = new App();
        app.run();
    }

    /**
     * Main body of app, user is gonna insert queries and changes are gonna be showed
     * @author Joshua Arrazola
     * */
    public void run(){
        boolean runFlag = true;

        // Here the user is gonna insert the queries
        do {
            try{
                System.out.println(Parser.parseQuery(Utilities.readBuffer()));
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }while (runFlag);

    }

}
