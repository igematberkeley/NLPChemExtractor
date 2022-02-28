package org.ucb.act.ROProjection;

import org.ucb.act.ROProjection.Reaction_Generator;
import org.ucb.act.ro.ROUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/** @author ArjunChandran **/
public class ChemoInformaticsPipeline {
    public HashMap<ArrayList, ArrayList<HashMap<ArrayList, String>>> main(String filepath_data, String filepath_RO) throws Exception{
        //1.) Instantiate all necessary classes
        //2.) Read and generate all possible reactions
        //3.) Read in and calculate all the mass differences for ROs
        //4.) Screen for RO reaction match based on mass difference
        //5.) Project RO on substrate to see if we get that product
        //6.) Screen based on projections and save valid reactions as output

        HashMap<ArrayList, ArrayList<HashMap<ArrayList, String>>> output = new HashMap<>();
        Reaction_Generator reac_gen = new Reaction_Generator();
        ROUtils utils = new ROUtils();
        ProjectionAnalysis proj_anl = new ProjectionAnalysis();
        Parser parser = new Parser();
        //this was step 1: instantiation and projection

        HashMap<ArrayList, HashMap<ArrayList, Double>> data_reactions = reac_gen.main(filepath_data); //find the reactions
        Set<String> ROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");//list of ROs
        HashMap<Double , ArrayList> RO_Mass_match = new HashMap<>();
        for(String RO: ROs){
            Double mass_diff = utils.get_mass_difference(RO); //for each RO, get the mass diff between reac/prod
            if (RO_Mass_match.keySet().contains(mass_diff)){//matches the mass diff of the other RO
                ArrayList smirks = RO_Mass_match.get(mass_diff);//gets the set of ROs with said mass diff
                smirks.add(RO);//adding it to the reactions list that have said mass diff
                RO_Mass_match.put(mass_diff, smirks);//back into the hashmap with our updated list
            } else { //if there's a mass diff we haven't seen before
                ArrayList<String> smirks = new ArrayList();
                smirks.add(RO);
                RO_Mass_match.put(mass_diff, smirks); //create and add the new mass diff to our hashmap
            }

        }
        //this was step 2&3: calculating the RO differences

        for(ArrayList location: data_reactions.keySet()){ //for each reaction
            HashMap <ArrayList, Double> possible_reaction_data = data_reactions.get(location);
            for(ArrayList<ArrayList> reaction: possible_reaction_data.keySet()){
                Double mass_diff = possible_reaction_data.get(reaction); //mass diff of the possible reaction
                ArrayList<String> rxn_ROs = RO_Mass_match.get(mass_diff); //all ROs that share the mass diff
                for (String RO: rxn_ROs){
                    ArrayList products = reaction.get(0); //get the pdt
                    ArrayList reactents= reaction.get(1); //get the reactants
                    if (reactents.size() == 1){ //only one molecule in the reactants
                        HashMap<Set<String>, HashMap<String, Set<String>>> stuff = proj_anl.oneMoleculeRun(??,rxn_ROs); //incorrect types to project the RO onto the reactions
                    } else if (reactents.size() == 2){

                    }
                }
            }
        }


    return output;
    }
}
