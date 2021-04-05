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
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Parser {

  /** Creates a hashmap with key sentence ID, value another Hashmap with key chemname, value smiles (soon changing to Inchi) */
  public HashMap<String, HashMap<String, String>> csvRun(String fileName) throws Exception {
    
    HashMap<String, HashMap<String, String>> listOfChemicalsSentenceID = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    br.readLine(); // read and skip the first line
    String sentence;
    String smile = null;
    String name = null;
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
      int error=0;
      //Iterate over all smiles to convert in inchis
      for (int i = 0; i < names.length; i++){
          //clean up chemical names and putting everything in lower case
          name=names[i].replaceAll("[\"]", "").replaceAll("%20", " ").replaceAll(" ", "").toLowerCase();
          smile= smiles[i].replaceAll("[\"]", "").replaceAll(" ", "");
          String onlyInchi = smileInchiConverter(smile);
          if (onlyInchi==null) continue;
          if (listOfChemicalsSentence.containsValue(onlyInchi)) continue;
          listOfChemicalsSentence.put(name,onlyInchi);
      listOfChemicalsSentenceID.put(id, listOfChemicalsSentence);
    } 
  }
    return listOfChemicalsSentenceID;
}

  /** Creates a hashmap with key RO name, value RO. */
  public Set<String> RORun(String fileName) throws Exception {
      
    Set<String> ros = new HashSet<>();
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    br.readLine(); // skip the first line
    String sentence;
    while ((sentence = br.readLine()) != null) {
              
        String[] columns = sentence.split("\t");
        String ro = columns[2];
        ros.add(ro);
    } 
    return ros;
  }
   
   
   public String smileInchiConverter(String smile) throws Exception {
     
    Molecule molecule = MolImporter.importMol(smile);
    String inchi =null;
    //Standardizer standardizer = new Standardizer("setabsolutestereo");
    //standardizer.standardize(molecule);
    try{
        inchi = MolExporter.exportToFormat(molecule, "inchi:AuxNone,Woff,SAbs");
    }catch(Exception e){
        return null;
    }
     return inchi;
    }
}

