package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.IOException;

public class Utilities {
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
}
