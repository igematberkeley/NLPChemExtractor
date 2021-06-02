package org.ucb.act.ROProjection;


import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import org.ucb.act.ro.Combination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/** @author ArjunChandran **/

public class Reaction_Generator{
    ///Returns Hashmap with two arrays with the key array containing sentence index start and end vals and the second array containing reaction combos
    public HashMap<ArrayList, ArrayList<String[]>> naive_generate(String filepath, int window_size) throws Exception {
            HashMap<ArrayList, ArrayList<String[]>> output = new HashMap();
            Parser parser = new Parser();
            HashMap<String, HashMap<String, String>> data = parser.csvRun(filepath);
            ///This and below can hopefully be replaced with the sliding window function for computational efficency
            Combination combiner = new Combination();

            for(int i = window_size; i < data.size(); i++){
                ArrayList<String> smiles_in_window = new ArrayList<>();

                for(int j = i; j <= window_size; j++){
                    HashMap<String, String> chemicals = data.get(Integer.toString(j));
                    Collection<String> smiles = chemicals.values();
                    for(String smile: smiles){
                        smiles_in_window.add(smile);
                    }
                }

                String[] bag_of_chemicals = new String[smiles_in_window.size()];
                int index = 0;

                for(String smile: smiles_in_window){
                    bag_of_chemicals[index] = smile;
                    index = index + 1;
                }

                ///Here we can consider expanding the length of chemical reactions if needed.
                List<String[]> reactions1 = combiner.generate(bag_of_chemicals, 2);
                List<String[]> reactions2 = combiner.generate(bag_of_chemicals, 3);
                ArrayList<String[]> reactions = new ArrayList();
                for(String[] reaction: reactions1){
                    reactions.add(reaction);
                }

                for(String[] reaction: reactions2){
                    reactions.add(reaction);
                }

                int start = i;
                int end = i + window_size;
                ArrayList indexes = new ArrayList();
                indexes.add(start);
                indexes.add(end);
                output.put(indexes, reactions);
            }

            return output;
    }

    public HashMap<ArrayList, Double> generate_mass_diff(String[] reaction_combos) throws MolFormatException {
        ArrayList<Molecule> rxnmolecules = new ArrayList<>();
        HashMap<ArrayList, Double> outputs = new HashMap<>();

        for(String i: reaction_combos){
           Molecule mol =  MolImporter.importMol(i);
           rxnmolecules.add(mol);
        }

        //Here we have an ArrayList containing a list of our reactions where each reaction
        //is an arraylist of arraylists representing our reactents and products
        ArrayList<ArrayList<ArrayList>> reactions = new ArrayList();
        //Here we calculate the mass_diff for each combination
        int i = 0;

        while (i < reaction_combos.length - 1) {
            int counter = i;
            boolean prod = false;

            for (Molecule molecule : rxnmolecules) {
                ArrayList<ArrayList> rxn = new ArrayList<>();
                ArrayList reactents = new ArrayList();
                ArrayList products = new ArrayList();
                if (counter == 0 & !prod) {
                    reactents.add(molecule);
                    prod = true;
                }
                if (counter == 1 & !prod){
                    reactents.add(molecule);
                    counter = counter - 1;
                }
                if(prod){
                    products.add(molecule);
                }
                rxn.add(reactents);
                rxn.add(products);
                reactions.add(rxn);

            }

            i = i + 1;

        }

        for(ArrayList<ArrayList> rxn: reactions){
            ArrayList<Molecule> products = rxn.get(1);
            ArrayList<Molecule> reactents = rxn.get(0);
            double product_mass = 0;
            double reactent_mass = 0;

            for(Molecule m: products){
                product_mass = product_mass + m.getExactMass();
            }

            for(Molecule m: reactents){
                reactent_mass = reactent_mass + m.getExactMass();
            }

            double mass_diff = product_mass - reactent_mass;
            outputs.put(rxn, mass_diff);
        }
        return outputs;
    }

    public HashMap<ArrayList, HashMap<ArrayList, Double>> main(String filepath) throws Exception{
        int window_size = 4;
        HashMap<ArrayList, HashMap<ArrayList, Double>> output = new HashMap<>();
        HashMap<ArrayList, ArrayList<String[]>> location_reaction_combos = naive_generate(filepath, window_size);
        ArrayList<ArrayList> keys = new ArrayList(location_reaction_combos.keySet());
        for (ArrayList key: keys){
            for(String[] reaction: location_reaction_combos.get(key)) {
                HashMap<ArrayList, Double> data = generate_mass_diff(reaction);
                output.put(key, data);
            }
        }
        return output;
    }
}