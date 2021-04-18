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

        HashMap<ArrayList, HashMap<ArrayList, Double>> data_reactions = reac_gen.main(filepath_data);
        Set<String> ROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        HashMap<Double , ArrayList> RO_Mass_match = new HashMap<>();
        for(String RO: ROs){
            Double mass_diff = utils.get_mass_difference(RO);
            if (RO_Mass_match.keySet().contains(mass_diff)){
                ArrayList smirks = RO_Mass_match.get(mass_diff);
                smirks.add(RO);
                RO_Mass_match.put(mass_diff, smirks);
            } else {
                ArrayList<String> smirks = new ArrayList();
                smirks.add(RO);
                RO_Mass_match.put(mass_diff, smirks);
            }

        }

        for(ArrayList location: data_reactions.keySet()){
            HashMap <ArrayList, Double> possible_reaction_data = data_reactions.get(location);
            for(ArrayList<ArrayList> reaction: possible_reaction_data.keySet()){
                Double mass_diff = possible_reaction_data.get(reaction);
                ArrayList<String> rxn_ROs = RO_Mass_match.get(mass_diff);
                for (String RO: rxn_ROs){
                    ArrayList products = reaction.get(0);
                    ArrayList reactents= reaction.get(1);
                    if (reactents.size() == 1){
                        HashMap<Set<String>, HashMap<String, Set<String>>> stuff = proj_anl.oneMoleculeRun(RO, reactents);

                    } else if (reactents.size() == 2){

                    }
                }
            }
        }


    return output;
    }
}
