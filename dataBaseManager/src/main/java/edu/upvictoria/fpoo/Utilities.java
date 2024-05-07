package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/*
* Class of Utilities to store common functions that doesn't fit in other classes
* @author Joshua Arrazola
* */
public class Utilities {
    private static Set<String> reservedWords = new HashSet<String>();
    private static Set<String> types = new HashSet<String>();
    private static final BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    /**
     * General function to read queries
     * */
    public static String readQuery(BufferedReader bf){
        String query = "";

        try  {
            query = bf.readLine();
        } catch (IOException e){
            System.out.println("Error leyendo query");
        }
        return query;
    }

    public static void fillReservedWords(){
        reservedWords.add("SELECT");
        reservedWords.add("INSERT");
        reservedWords.add("DELETE");
        reservedWords.add("FROM");
        reservedWords.add("INT");
        reservedWords.add("VARCHAR");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("UPDATE");
        reservedWords.add("SET");
        reservedWords.add("DATE");
        reservedWords.add("USE");
        reservedWords.add("ORDER");
        reservedWords.add("BY");
    }

    public static void fillTypes(){
        types.add("INT");
        types.add("VARCHAR");
        types.add("DATE");
        types.add("FLOAT");
        types.add("DOUBLE");
    }

    public static boolean isReservedWord(String word){
        return reservedWords.contains(word.toUpperCase());
    }

    public static boolean isType(String type){
        return types.contains(type.toUpperCase());
    }

    public static boolean hasValidChars(String str){
        return !(str.contains(".")|| str.contains("/")||
                str.contains("|")||str.contains("&"));
    }

    public static String readBuffer(){
        String query = "";
        do {
            if(query.isEmpty())
                System.out.println("Ingresa la query (ingresa 'x' si ya no hay mas ENTER): ");

            String creatingQuery = Utilities.readQuery(bf).trim();

            if (creatingQuery.equals("X")){
                return query;
            }
            else {
                if (!query.isEmpty()) {
                    query += " ";
                }
                query += creatingQuery;
            }
        }while (true);
    }
}
