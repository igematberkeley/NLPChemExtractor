/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;


import java.util.Arrays;
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
    public HashMap<Set<String>, HashMap<String, Set<String>>> oneMoleculeRun(Map<String, String> sentenceNameInchis, Set<String> namesROs) {

        //HashSet with names of chemicals from one sentence
        Set<String> names = sentenceNameInchis.keySet();


        Map<String, String> inchisNames = new HashMap<>();
        for(Map.Entry<String, String> entry : sentenceNameInchis.entrySet()){
            inchisNames.put(entry.getValue(), entry.getKey());
        }

        //HashMaps to link substrate, RO and product(s) for output
        HashMap<Set<String>, HashMap<String, Set<String>>> output = new HashMap<>();
        ROProjecter rOProjecter = new ROProjecter();

        //Iterate over all chemicals within one sentence
        for (String chemicalName : names) {
          HashMap<String, Set<String>> roProducts = new HashMap<>();
          // nad mostly as cofactor, making 2 substrate chemical reactions (nad/h+ other chemical)
          // since RO projection only takes into account the functional groups and not cofactors it is not necessary to include nad
          //if we include nad, we will only get falso positives or duplicate reactions
          if (chemicalName.contains("nad"))
            continue;
          //Since the output format is a HashMap containing an array at its value, an array for one substrate is created
          String[] inchisArray = {sentenceNameInchis.get(chemicalName)};
          String[] namesArray = {chemicalName};
          //inchisArray[0] = sentenceNameInchis.get(chemicalName);
          //namesArray[0] = chemicalName;

          //Project all ROs found in the database
          for (Map.Entry<String, String> entry : namesROs.entrySet()) {
            Set<String> pdts = new HashSet<>();
            String ro = entry.getValue();

            //Some substrates may throw an exception for RO projection, maybe due to their SMILES format
            //**Maybe some improvement can be done to decrease exception number
            try {
              //HashSet to collect any possible product
              pdts = rOProjecter.project(ro, inchisArray);
              //If no projection was succesfull, there is no need to continue in further analysis.
              if (pdts.isEmpty())continue;
            } catch (Exception e) {
              continue;
            }


            Set<String> listProductsNames = new HashSet<>();

            //Checks if the obtained products are chemicals within the sentence
            if(!sentenceNameInchis.values().containsAll(pdts)) continue;
            //Checking substrate != product
            for (String product : pdts) {
                if (!product.equals(inchisArray[0])){
                    listProductsNames.add(inchisNames.get(product));
                }
            }
            if (listProductsNames.size() > 0) roProducts.put(ro, listProductsNames);
          }

          if (roProducts.size() > 0){
              Set<String> namesSet = new HashSet<String>(Arrays.asList(namesArray));
              output.put(namesSet, roProducts);
          }
        }
        return output;
  }

     public HashMap<Set<String>, HashMap<String, Set<String>>> twoMoleculesRun(Map<String, String> sentenceNameInchis, HashMap<String, String> namesROs) {

        //HashSet with names of chemicals from one sentence
        Set<String> names = sentenceNameInchis.keySet();


        Map<String, String> inchisNames = new HashMap<>();
        for(Map.Entry<String, String> entry : sentenceNameInchis.entrySet()){
            inchisNames.put(entry.getValue(), entry.getKey());
        }


        HashMap<Set<String>, HashMap<String, Set<String>>> output = (HashMap)new HashMap<>();

        ROProjecter rOProjecter = new ROProjecter();

        //Generate a list with combinations of all posible pair of substrates within the sentece
        String[] names1 = names.<String>toArray(new String[names.size()]);
        //Uses class that creates combinations of subgroups with n number of species (in this case 2)
        List<String[]> combinations = Combination.generate(names1, 2);

        for (String[] chemicalNames : combinations) {
          //Make the array for the names and SMILEs of the pair of substrates
          String[] inchisArray = {sentenceNameInchis.get(chemicalNames[0]),sentenceNameInchis.get(chemicalNames[1])};
          String[] namesArray = {chemicalNames[0],chemicalNames[1]};


          HashMap<String, Set<String>> roProducts = new HashMap<>();

          //Project all ROs found in the database
          for (Map.Entry<String, String> entry : namesROs.entrySet()) {
            //HashSet to collect any possible product
            Set<String> pdts = new HashSet<>();
            //There are ROs that cant be projected in 2 substrates!!!!!!!!
            String ro = entry.getValue();
            //Some substrates may trhow an exception for RO projection, maybe due to their SMILES format
            //**Maybe some improvement be done to decrease exception number
            try {
              pdts = rOProjecter.project(ro, inchisArray);
              System.out.println(entry.getKey());
              if (pdts.isEmpty()) continue;
            } catch (Exception e) {
              continue;
            }


            Set<String> listProductsNames = new HashSet<>();

            //Checks if the obtained products are chemicals within the sentence
            if(!sentenceNameInchis.values().containsAll(pdts)) continue;
            //Checking substrate != product
            for (String product : pdts) {
              if (sentenceNameInchis.containsValue(product) && !product.equals(inchisArray[0]) && !product.equals(inchisArray[1]))
                listProductsNames.add(inchisNames.get(product));

            } 
            if (listProductsNames.size() > 0) roProducts.put(ro, listProductsNames);  
          }
          
          if(roProducts.size()>0){
              Set<String> namesSet = new HashSet<String>(Arrays.asList(namesArray));
              output.put(namesSet, roProducts);
          }
        } 
        return output;
    }
    
}
