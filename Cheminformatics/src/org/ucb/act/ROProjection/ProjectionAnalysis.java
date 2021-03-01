/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;


import org.ucb.act.ro.Combination;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.act.ro.ROProjecter;

/**
 *
 * @author jesusdelrio
 */
public class ProjectionAnalysis {
    
    //Method for considering a chemical reaction with only one substrate
    //RO projection in one chemical
    public HashMap<String[], HashMap<String, Set<String>>> oneMoleculeRun(Map<String, String> sentenceNameSmile, HashMap<String, String> namesROs, HashMap<String, String> namesInchis) {
        
        //HashSet with names of chemicals from one sentence
        Set<String> names = sentenceNameSmile.keySet();
        //HashSet to collect any possible product
        Set<String> pdts = new HashSet<>();
        //HashSet only viable products (chemicals found within the sentence)
        Set<String> productNames = new HashSet<>();
        
        //HashMaps to link substrate, RO and product(s) for output
        HashMap<String, Set<String>> roProducts = new HashMap<>();
        HashMap<String[], HashMap<String, Set<String>>> output = (HashMap)new HashMap<>();
        
        ROProjecter rOProjecter = new ROProjecter();
        
        //Iterate over all chemicals within one sentence
        for (String chemicalName : names) {
          // nad mostly as cofactor, making 2 substrate chemical reactions (nad/h+ other chemical)
          // since RO projection only takes into account the functional groups and not cofactors it is not necessary to include nad
          //if we include nad, we will only get falso positives or duplicate reactions
          if (chemicalName.contains("nad"))
            continue; 
          //Since the output format is a HashMap containing an array at its value, an array for one substrate is created
          String[] smilesArray = new String[1];
          String[] namesArray = new String[1];
          smilesArray[0] = sentenceNameSmile.get(chemicalName);
          namesArray[0] = chemicalName;
          
          //Project all ROs found in the database
          for (Map.Entry<String, String> entry : namesROs.entrySet()) {     
            String ro = entry.getValue();
            //Some substrates may trhow an exception for RO projection, maybe due to their SMILES format
            //**Maybe some improvement be done to decrease exception number
            try {
              pdts = rOProjecter.project(ro, smilesArray);
              //If no projection was succesfull, there is no need to continue in further analysis.
              if (pdts.isEmpty())
                continue; 
            } catch (Exception e) {
              continue;
            } 
            
            //The products in an RO projections are in inchi format, they are converted to their chemical name
            for (String inchi : pdts) {
              if (namesInchis.containsKey(inchi)) {
                String name = namesInchis.get(inchi);
                productNames.add(name);
              } 
            } 
            //The inchi-name database can be a limiting factor for obtaining all products
            if (productNames.isEmpty()) continue; 
            
            Set<String> listProducts = new HashSet<>();
            
            //This loop checks if the obtained products are chemicals within the sentence, validating a reaction in that sentence
            for (String product : productNames) {
              if (names.contains(product) && !product.equals(namesArray[0]))
                listProducts.add(product); 
            } 
            if (listProducts.size() > 0) {
              roProducts.put(ro, listProducts);
              output.put(namesArray, roProducts);
            } 
          } 
        } 
        return output;
  }
    
     public HashMap<String[], HashMap<String, Set<String>>> twoMoleculesRun(Map<String, String> sentenceNameSmile, HashMap<String, String> namesROs, HashMap<String, String> namesInchis) {
        
        //HashSet with names of chemicals from one sentence
        Set<String> names = sentenceNameSmile.keySet();
        //HashSet to collect any possible product
        Set<String> pdts = new HashSet<>();
        //HashSet only viable products (chemicals found within the sentence)
        Set<String> productNames = new HashSet<>();
        
        //HashMaps to link substrate, RO and product(s) for output
        HashMap<String, Set<String>> roProducts = new HashMap<>();
        HashMap<String[], HashMap<String, Set<String>>> output = (HashMap)new HashMap<>();
        
        ROProjecter rOProjecter = new ROProjecter();
        
        //Generate a list with combinations of all posible pair of substrates within the sentece
        String[] names1 = names.<String>toArray(new String[names.size()]);
        //Uses class that creates combinations of subgroups with n number of species (in this case 2)
        List<String[]> combinations = Combination.generate(names1, 2);
        
        for (String[] chemicalNames : combinations) {
          //Make the array for the names and SMILEs of the pair of substrates 
          String[] smilesArray = new String[2];
          String[] namesArray = new String[2];
          smilesArray[0] = sentenceNameSmile.get(chemicalNames[0]);
          smilesArray[1] = sentenceNameSmile.get(chemicalNames[1]);
          namesArray[0] = sentenceNameSmile.get(chemicalNames[0]);
          namesArray[1] = sentenceNameSmile.get(chemicalNames[1]);
          
          //Project all ROs found in the database
          for (Map.Entry<String, String> entry : namesROs.entrySet()) {
            String ro = entry.getValue();
            //Some substrates may trhow an exception for RO projection, maybe due to their SMILES format
            //**Maybe some improvement be done to decrease exception number
            try {
              pdts = rOProjecter.project(ro, smilesArray);
              if (pdts.isEmpty())
                continue; 
            } catch (Exception e) {
              continue;
            } 
            //The products in an RO projections are in inchi format, they are converted to their chemical name
            for (String inchi : pdts) {
              if (namesInchis.containsKey(inchi)) {
                String name = namesInchis.get(inchi);
                productNames.add(name);
              } 
            }
            //The inchi-name database can be a limiting factor for obtaining all products
            if (productNames.isEmpty())
              continue; 
            Set<String> listProducts = new HashSet<>();
            //This loop checks if the obtained products are chemicals within the sentence, validating a reaction in that sentence
            for (String product : productNames) {
              if (names.contains(product) && !product.equals(namesArray[0]) && !product.equals(namesArray[1]))
                listProducts.add(product); 
            } 
            if (listProducts.size() > 0) {
              roProducts.put(ro, listProducts);
              output.put(namesArray, roProducts);
            } 
          } 
        } 
        return output;
    }
    
}
