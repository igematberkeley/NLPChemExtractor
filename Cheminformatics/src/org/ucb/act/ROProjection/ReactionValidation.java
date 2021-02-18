/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.act.utils.ChemAxonUtils;

/**
 *
 * @author jesusdelrio
 */
public class ReactionValidation {

    public static void main(String[] args) throws Exception {

        Parser Parser = new Parser();
        ProjectionAnalysis rOProjection = new ProjectionAnalysis();
        
        

        
        ChemAxonUtils.license();
        
        // HashMap for extracted chemicals per sentece. Key->doi and sentence #, Value->HashMap (key->name, value->SMILES)
        HashMap<String,HashMap<String,String>> listOfChemicals = Parser.csvRun("/Users/jesusdelrio/Downloads/test_data3.csv");
        // Key->Name, Value->RO
        HashMap<String, String> namesROs = Parser.hashMapRun("/Users/jesusdelrio/Downloads/2015_01_16-ROPruner_hchERO_list.txt");
        // Key->Inchi, Value->Name
        HashMap<String, String> namesInchis = Parser.inchiRun("/Users/jesusdelrio/Project5/act_files/good_chems.txt");
        
        // Initiate some Hashmaps
        HashMap<String[],HashMap<String,Set<String>>> outputSingleMolecule = new HashMap<String[],HashMap<String,Set<String>>>();
        HashMap<String[],HashMap<String,Set<String>>> outputTwoMolecules = new HashMap<String[],HashMap<String,Set<String>>>();
        HashMap<String,HashMap<String[],HashMap<String,Set<String>>>> outputWithID = new HashMap<String,HashMap<String[],HashMap<String,Set<String>>>>();
        ArrayList listOutputs = new ArrayList();
       
  
       
        int c=0;
       
        Set<String> idSet = listOfChemicals.keySet();
        for(String id:idSet){
            
            c=c+1;
       
            Map<String,String> sentence = listOfChemicals.get(id);
            
            
            if (sentence.containsKey("") || sentence.size()==1) continue;
  
            outputSingleMolecule = rOProjection.oneMoleculeRun(sentence, namesROs, namesInchis);
            
            
            if (outputSingleMolecule.isEmpty()){
            outputTwoMolecules = rOProjection.twoMoleculesRun(sentence, namesROs, namesInchis);
                if(outputTwoMolecules.size()>0){
                    //listOutputs.add(outputTwoMolecules);
                    outputWithID.put(id,outputTwoMolecules);
                    
                }
            }else{
                //listOutputs.add(outputSingleMolecule);
                outputWithID.put(id,outputSingleMolecule);
            }
            
            if(c%1000==0){
                System.out.println(c);
            }
            
        }
        
        
         
        File file = new File ("/Users/jesusdelrio/Downloads/outputCheminformatics_test.csv");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        
        
        writer.write("lit_id, index, Reactant(s),RO, Product(s) \n");
         
        Iterator<Map.Entry<String,HashMap<String[],HashMap<String,Set<String>>>>> sentences = outputWithID.entrySet().iterator();
         
         while(sentences.hasNext()){
             Map.Entry<String,HashMap<String[],HashMap<String,Set<String>>>> sentence = sentences.next();
             String id = sentence.getKey();
             String[] numIDindex =id.split(",");
             
             Iterator<Map.Entry<String[],HashMap<String,Set<String>>>> substratesAllReactions = sentence.getValue().entrySet().iterator();
             
             while(substratesAllReactions.hasNext()){
                 Map.Entry<String[],HashMap<String,Set<String>>> substratesOneReaction = substratesAllReactions.next();
                 String substrates = String.join(",",substratesOneReaction.getKey());
                 
                 Iterator<Map.Entry<String,Set<String>>> allROs = substratesOneReaction.getValue().entrySet().iterator();
                 
                 while(allROs.hasNext()){
                     Map.Entry<String,Set<String>> oneRO =allROs.next();
                     String ro = oneRO.getKey();
                     
                     String products=String.join(",",oneRO.getValue());
                     writer.write(numIDindex[0]+","+numIDindex[1]+",\""+substrates+"\","+ro+",\""+products+"\"\n");
                     
                      int a=6;
                 }
             }   
         }
         
         
             
             
             
        
             
             int a=6;
         }
        
    }


