package edu.upvictoria.fpoo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class TablaTemp {
    public static void main(String[] args) {
        String condicional = "";
        String tableDir = "";

        String temporalAuxInfoPath = (new File("")).getAbsolutePath() + "/temporalAuxInfo.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(temporalAuxInfoPath))){
            String line = br.readLine();
            String[] lineBrk = line.split(",");
            condicional = lineBrk[0];
            tableDir = lineBrk[1];
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        ArrayList<String> tabla = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(tableDir))){
            String line = br.readLine();
            tabla.add(line);
            while ((line = br.readLine()) != null) {
                String[] arrBrk = line.split(","); 
                if(String.valueOf(arrBrk[1]).equals("JOSHUA"))
                    tabla.add(line);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        try(BufferedWriter bw = new BufferedWriter(new FileWriter((new File("")).getAbsolutePath() + "/temporalAuxInfo.csv"))){
            for(String row : tabla){
                bw.write(row);
                bw.newLine();
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}