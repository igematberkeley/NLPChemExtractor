package org.ucb.act.ROProjection;

import chemaxon.struc.Molecule;
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

        //Instantiate necessary classes
        HashMap<ArrayList, ArrayList<HashMap<ArrayList, String>>> output = new HashMap<>();
        Reaction_Generator reac_gen = new Reaction_Generator();
        ROUtils utils = new ROUtils();
        Molecule_Projector proj_anl = new Molecule_Projector();
        Parser parser = new Parser();

        //Generate rection combinations
        HashMap<ArrayList, HashMap<ArrayList, Double>> data_reactions = reac_gen.main(filepath_data);
        //Load known ROs
        Set<String> ROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        //Instantiate an empty HashMap that will match known ROs to their mass differences
        HashMap<Double , ArrayList> RO_Mass_match = new HashMap<>();
        //Here we iterate through every loaded RO and calculate the mass difference and put it into our HashMap
        for(String RO: ROs){
            Double mass_diff = utils.get_mass_difference(RO);
            //If the mass_difference already exists in the key set we add the RO to that key's corresponding array
            //Otherwise we add an entirely new key value pair
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

        //iterate by location to match reactions with their ROs
        for(ArrayList location: data_reactions.keySet()){
            HashMap <ArrayList, Double> possible_reaction_data = data_reactions.get(location);
            for(ArrayList<ArrayList> reaction: possible_reaction_data.keySet()){
                Double mass_diff = possible_reaction_data.get(reaction);
                ArrayList<String> rxn_ROs = RO_Mass_match.get(mass_diff);
                String right_RO = null;
                for (String RO: rxn_ROs){
                    ArrayList<Molecule> products = reaction.get(0);
                    ArrayList<Molecule> reactents= reaction.get(1);
                    Boolean stuff = proj_anl.MoleculeRxn(RO, reactents, products);
                    if (stuff) {
                        right_RO = RO;
                        break;
                    }
                }
                HashMap<ArrayList, String> output_entry = new HashMap<>();
                output_entry.put(reaction, right_RO);
                if(output.keySet().contains(location)) {
                    ArrayList rxns = output.get(location);
                    rxns.add(output_entry);
                    output.replace(location, output.get(location), rxns);
                }
                else {
                    ArrayList rxns = new ArrayList();
                    rxns.add(output_entry);
                    output.put(location, rxns);
                }
            }
        }


    return output;
    }
}
