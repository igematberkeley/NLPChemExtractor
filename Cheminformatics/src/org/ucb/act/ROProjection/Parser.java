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
import chemaxon.formats.MolConverter;
import chemaxon.formats.MolExporter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Parser {

  /** Creates a hashmap with key sentence ID, value another Hashmap with key chemname, value smiles (soon changing to Inchi) */
  public HashMap<String, HashMap<String, String>> csvRun(String fileName) throws Exception {
    //New feature: converting SMILES -> inchis
    MolConverter.Builder converter = new MolConverter.Builder();
    HashMap<String, HashMap<String, String>> listOfChemicalsSentenceID = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    br.readLine(); // read and skip the first line
    String sentence;
    String smile = null;
    String name = null;
    Set<String> totalSmiles = new HashSet();
    Set<String> totalInchis = new HashSet();
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
      int c=0;
      int error=0;
      for (int i = 0; i < names.length; i++)
          //clean up chemical names and putting everything in lower case
          name=names[i].replaceAll("[\"]", "").replaceAll("%20", " ").replaceAll(" ", "").toLowerCase();
          smile= smiles[c].replaceAll("[\"]", "").replaceAll(" ", "");
          totalSmiles.add(smile);
          c++;
          //Converting SMILES into inchis
          InputStream roStream = new ByteArrayInputStream(smile.getBytes());
          converter.addInput(roStream, "smiles");
          ByteArrayOutputStream inchiStream = new ByteArrayOutputStream();
          converter.setOutput(inchiStream, "InChI");
          converter.setOutputFlags(MolExporter.TEXT );
          try{
          MolConverter mc = converter.build();
          mc.convert();
          mc.close();
          }catch(Exception e){
              continue;
          }
          String inchi = new String(inchiStream.toByteArray());
          String[] onlyInchi=inchi.split("AuxInfo");
          totalInchis.add(onlyInchi[0]);
          listOfChemicalsSentence.put(name,onlyInchi[0]);
      listOfChemicalsSentenceID.put(id, listOfChemicalsSentence);
    } 
    int succesfulInchi= totalInchis.size()*100/totalSmiles.size();
    System.out.println("Succesful SMILE->Inchi conversion: "+succesfulInchi+"%");
    return listOfChemicalsSentenceID;
  }

  /** Creates a hashmap with key RO name, value RO. */
  public HashMap<String, String> RORun(String fileName) throws Exception {
    
    //New feature: converting SMILES -> inchis
    MolConverter.Builder converter = new MolConverter.Builder();
    
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

