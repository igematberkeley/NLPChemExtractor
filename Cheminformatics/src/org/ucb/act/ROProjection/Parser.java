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

  /** Creates a hashmap with key sentence ID, value another Hashmap with key chemname, value smiles (soon changing to Inchi) */
  public HashMap<String, HashMap<String, String>> csvRun(String fileName) throws Exception {
    HashMap<String, HashMap<String, String>> listOfChemicalsSentenceID = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    br.readLine(); // read and skip the first line
    String sentence;
    while ((sentence = br.readLine()) != null) {
      if (sentence.equals(""))
        continue;
      HashMap<String, String> listOfChemicalsSentence = new HashMap<>();
      //Omits detecting a , within "" as column separation. Only commas outside "" are used for separating columns
      String[] columns = sentence.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
      //Rows with incomplete columns or with no chemical_smiles are skipped
      if (columns.length < 12 || columns[10].equals(""))
        continue;
      // id is lit_id + sentence index. names are chemical names in a list, smiles are chemical smiles
      String id = columns[0] + "," + columns[1];
      String[] names = columns[9].split(",,");
      String[] smiles = columns[10].split(",,");
      for (int i = 0; i < names.length; i++)
          //clean up chemical names and putting everything in lower case
          listOfChemicalsSentence.put(names[i].replaceAll("[\"]", "").replaceAll("%20", " ").replaceAll(" ", "").toLowerCase(), smiles[i].replaceAll("[\"]", "").replaceAll(" ", ""));
      listOfChemicalsSentenceID.put(id, listOfChemicalsSentence);
    } 
    return listOfChemicalsSentenceID;
  }

  /** Creates a hashmap with key RO name, value RO. */
  public HashMap<String, String> RORun(String fileName) throws Exception {
    HashMap<String, String> namesROs = new HashMap<>();
    
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    br.readLine(); // skip the first line
    String sentence;
    while ((sentence = br.readLine()) != null) {
      String[] columns = sentence.split("\t");
      String name = columns[1];
      String ro = columns[2];
      namesROs.put(name, ro);
    } 
    return namesROs;
  }
   
   
   public HashMap<String, String> inchiRun(String fileName) throws Exception {
    HashMap<String, String> namesInchis = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    br.readLine(); // skip the first line
    String sentence;
    while ((sentence = br.readLine()) != null) {
      String[] columns = sentence.split("\t");
      String name = columns[1];
      String inchi = columns[2];
      // remove apostrophe from inchi name (from goodChems)
      // clean up chemical names and putting everything in lower case
      namesInchis.put(inchi.replaceAll("\"", ""), name.replaceAll(" ", "").toLowerCase());
    } 
    return namesInchis;
  }
  
}

