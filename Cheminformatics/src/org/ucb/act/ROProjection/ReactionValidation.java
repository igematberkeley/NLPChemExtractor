/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.ucb.act.utils.ChemAxonUtils;

/**
 *
 * @author jesusdelrio
 */
public class ReactionValidation {

    public static void main(String[] args) throws Exception {
        
        //Class with methods to parse chemical mining results csv, name-ro txt and name-inchi txt.
        Parser Parser = new Parser();
        //Class to project ROs and validate a reaction. Contains 1 methods to validate 1 or 2 substrate reactions.
        ProjectionAnalysis projectionAnalysis = new ProjectionAnalysis();
        
        ChemAxonUtils.license();
        
        // Key->doi and sentence #, Value->HashMap(key->name, value->SMILES)
        HashMap<String,HashMap<String,String>> listOfChemicals = Parser.csvRun("./test_data1.csv");
        // Key->Name, Value->RO
        HashMap<String, String> namesROs = Parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        // Key->Inchi, Value->Name
        HashMap<String, String> namesInchis = Parser.inchiRun("./good_chems.txt");
        
        //HashMaps to record validated reactions considering 1 or 2 substrates:
        //Key->Substrate, Value->HashMap(key->RO, value->List of products)
        HashMap<String[],HashMap<String,Set<String>>> outputSingleMolecule = new HashMap<String[],HashMap<String,Set<String>>>();
        //Key->List of substrates, Value->HashMap(key->RO, value->list of products)
        HashMap<String[],HashMap<String,Set<String>>> outputTwoMolecules = new HashMap<String[],HashMap<String,Set<String>>>();
        
        //Output HashMap:
        //Key->doi,sentence#, Value->HashMap(Key->List of substrates, Value->HashMap(key->RO, value->list of products))
        HashMap<String,HashMap<String[],HashMap<String,Set<String>>>> outputWithID = new HashMap<String,HashMap<String[],HashMap<String,Set<String>>>>();
        

        //Iteration of senteces from all papers
        Set<String> idSet = listOfChemicals.keySet();
        for(String id:idSet){
            
            Map<String,String> sentence = listOfChemicals.get(id);
            if (sentence.containsKey("") || sentence.size()==1) continue;
            
            if(sentence.containsValue("InChI=1S/C6H9N3O/c7-5(3-10)1-6-2-8-4-9-6/h2-5H,1,7H2,(H,8,9)/t5-/m0/s1")){
                int a =0;
            }
  
            // Call RO projection method considering only ONE molecule as substrate
            // Output contains a list of possible reactions, substrates can be different for each reaction
            outputSingleMolecule = projectionAnalysis.oneMoleculeRun(sentence, namesROs);
            
            
            //If no reactions were extraced by considering ONE molecule, try considering a PAIR of molecules as substrates
            if (outputSingleMolecule.isEmpty()){
                //To save some time I am skipping two substrate analysis
                //outputTwoMolecules = projectionAnalysis.twoMoleculesRun(sentence, namesROs);
                //if(outputTwoMolecules.size()>0)
                    //outputWithID.put(id,outputTwoMolecules);
            }else{
                outputWithID.put(id,outputSingleMolecule);
            }
        }
        convertToCSV(outputWithID);  
    }
    
    
    
  
    public static void convertToCSV(HashMap<String,HashMap<String[],HashMap<String,Set<String>>>> outputWithID) throws Exception{
        
        File file = new File ("./outputCheminformatics_test6.csv");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
       
        writer.write("lit_id, index, Reactant(s),RO, Product(s) \n");
        Iterator<Map.Entry<String,HashMap<String[],HashMap<String,Set<String>>>>> sentences = outputWithID.entrySet().iterator();
         
        //Iterate for all validated sentences
         while(sentences.hasNext()){
             Map.Entry<String,HashMap<String[],HashMap<String,Set<String>>>> sentence = sentences.next();
             String id = sentence.getKey();
             String[] numIDindex =id.split(",");
             
             Iterator<Map.Entry<String[],HashMap<String,Set<String>>>> allSubstratesFromSentence = sentence.getValue().entrySet().iterator();
             
             //Iterate all single or pair of substrates per sentence
             while(allSubstratesFromSentence.hasNext()){
                 Map.Entry<String[],HashMap<String,Set<String>>> substratesOneReaction = allSubstratesFromSentence.next();
                 String substrates = String.join(",",substratesOneReaction.getKey());
                 
                 Iterator<Map.Entry<String,Set<String>>> allROs = substratesOneReaction.getValue().entrySet().iterator();
                 
                 //Iterate all ROs (with respective product) that fit each single or pair of substrates
                 while(allROs.hasNext()){
                     Map.Entry<String,Set<String>> oneRO =allROs.next();
                     String ro = oneRO.getKey();
                     String products=String.join(",",oneRO.getValue());
                     
                     //Write row in csv for unique RO and product(s)
                     writer.write(numIDindex[0]+","+numIDindex[1]+",\""+substrates+"\","+ro+",\""+products+"\"\n");
                }
            }   
        }
         
         writer.close();
    }    
}
        
    


