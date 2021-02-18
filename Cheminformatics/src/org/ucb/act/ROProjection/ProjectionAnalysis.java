/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.act.ro.Combination;
import org.ucb.act.ro.ROProjecter;

/**
 *
 * @author jesusdelrio
 */
public class ProjectionAnalysis {
    
    public HashMap<String[], HashMap<String, Set<String>>> oneMoleculeRun(Map<String, String> sentenceNameSmile, HashMap<String, String> namesROs, HashMap<String, String> namesInchis) {
        Set<String> names = sentenceNameSmile.keySet();
        Set<String> pdts = new HashSet<>();
        Set<String> productNames = new HashSet<>();
        HashMap<String, Set<String>> roProducts = new HashMap<>();
        HashMap<String[], HashMap<String, Set<String>>> output = (HashMap)new HashMap<>();
        ROProjecter rOProjecter = new ROProjecter();
        for (String chemicalName : names) {
          if (chemicalName.contains("nad"))
            continue; 
          String[] smilesArray = new String[1];
          String[] namesArray = new String[1];
          smilesArray[0] = sentenceNameSmile.get(chemicalName);
          namesArray[0] = chemicalName;
       
          for (Map.Entry<String, String> entry : namesROs.entrySet()) {     
            String ro = entry.getValue();
            try {
              pdts = rOProjecter.project(ro, smilesArray);
              if (pdts.isEmpty())
                continue; 
            } catch (Exception e) {
              continue;
            } 
            for (String inchi : pdts) {
              if (namesInchis.containsKey(inchi)) {
                String name = namesInchis.get(inchi);
                productNames.add(name);
              } 
            } 
            if (productNames.isEmpty())
              continue; 
            Set<String> listProducts = new HashSet<>();
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
    Set<String> names = sentenceNameSmile.keySet();
    Set<String> pdts = new HashSet<>();
    Set<String> productNames = new HashSet<>();
    HashMap<String, Set<String>> roProducts = new HashMap<>();
    HashMap<String[], HashMap<String, Set<String>>> output = (HashMap)new HashMap<>();
    String[] names1 = names.<String>toArray(new String[names.size()]);
    ROProjecter rOProjecter = new ROProjecter();
    List<String[]> combinations = Combination.generate(names1, 2);
    for (String[] chemicalNames : combinations) {
      String[] smilesArray = new String[2];
      String[] namesArray = new String[2];
      smilesArray[0] = sentenceNameSmile.get(chemicalNames[0]);
      smilesArray[1] = sentenceNameSmile.get(chemicalNames[1]);
      namesArray[0] = sentenceNameSmile.get(chemicalNames[0]);
      namesArray[1] = sentenceNameSmile.get(chemicalNames[1]);
      for (Map.Entry<String, String> entry : namesROs.entrySet()) {
        String ro = entry.getValue();
        try {
          pdts = rOProjecter.project(ro, smilesArray);
          if (pdts.isEmpty())
            continue; 
        } catch (Exception e) {
          continue;
        } 
        for (String inchi : pdts) {
          if (namesInchis.containsKey(inchi)) {
            String name = namesInchis.get(inchi);
            productNames.add(name);
          } 
        } 
        if (productNames.isEmpty())
          continue; 
        Set<String> listProducts = new HashSet<>();
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
