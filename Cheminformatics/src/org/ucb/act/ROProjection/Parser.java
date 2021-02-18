/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;

/**
 *
 * @author jesusdelrio
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    
  public HashMap<String, HashMap<String, String>> csvRun(String fileName) throws Exception {
    ArrayList<HashMap<String, HashMap<String, String>>> listOfChemicals = new ArrayList<>();
    HashMap<String, HashMap<String, String>> listOfChemicalsSentenceID = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    int c = 0;
    String sentence;
    while ((sentence = br.readLine()) != null) {
      if (sentence.equals(""))
        continue; 
      HashMap<String, String> listOfChemicalsSentence = new HashMap<>();
      if (c == 0) {
        c = 1;
        continue;
      } 
      c++;
      String[] columns = sentence.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
      if (columns.length < 13)
        continue; 
      String id = columns[1] + "," + columns[2];
      if (columns[10].equals(""))
        continue; 
      String[] names = columns[10].split(",,");
      String[] smiles = columns[11].split(",,");
      for (int i = 0; i < names.length; i++)
        listOfChemicalsSentence.put(names[i].replaceAll("[\"]", "").replaceAll("%20", "").replaceAll(" ", "").toLowerCase(), smiles[i].replaceAll("[\"]", "").replaceAll(" ", "")); 
      listOfChemicalsSentenceID.put(id, listOfChemicalsSentence);
    } 
    return listOfChemicalsSentenceID;
  }
  
  
   public HashMap<String, String> hashMapRun(String fileName) throws Exception {
    HashMap<String, String> namesROs = new HashMap<>();
    
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    int c = 0;
    String sentence;
    while ((sentence = br.readLine()) != null) {
      if (c == 0) {
        c = 1;
        continue;
      } 
      String[] columns = sentence.split("\t");
      String name = columns[1];
      String ro = columns[2];
      int i = 0;
      namesROs.put(name, ro);
    } 
    return namesROs;
  }
   
   
   public HashMap<String, String> inchiRun(String fileName) throws Exception {
    HashMap<String, String> namesInchis = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    int c = 0;
    String sentence;
    while ((sentence = br.readLine()) != null) {
      if (c == 0) {
        c = 1;
        continue;
      } 
      String[] columns = sentence.split("\t");
      String name = columns[1];
      String inchi = columns[2];
      int i = 0;
      namesInchis.put(inchi.replaceAll("\"", ""), name.replaceAll(" ", "").toLowerCase());
    } 
    return namesInchis;
  }
  
}

