package org.ucb.act.ro;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;

import java.util.HashMap;

import static java.lang.StrictMath.abs;

/** @author ArjunChandran **/

public class ROUtils{
    public HashMap<String, Molecule[]> explode(String ro) throws Exception {
        RxnMolecule rxn = RxnMolecule.getReaction(MolImporter.importMol(ro));
        Molecule[] substrates = rxn.getReactants();
        Molecule[] products = rxn.getProducts();
        HashMap rxn_species = new HashMap<String, Molecule[]>();
        rxn_species.put("Reactants", substrates);
        rxn_species.put("Products", products);
        return rxn_species;

    }

    public double get_mass_difference(String ro) throws Exception {
        HashMap<String, Molecule[]> species = explode(ro);
        Molecule[] reactants = species.get("Reactants");
        Molecule[] products = species.get("Products");
        double reac_mass = 0;
        double prod_mass = 0;
        for(int i = 0; i < reactants.length; i++){
            Molecule curr_species = reactants[i];
            double mass = curr_species.getExactMass();
            reac_mass = reac_mass + mass;
        }
        for(int i = 0; i < products.length; i++){
            Molecule curr_species = products[i];
            double mass = curr_species.getExactMass();
            prod_mass = prod_mass + mass;
        }
        return abs(reac_mass - prod_mass);
    }

    
}