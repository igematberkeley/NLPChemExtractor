package newChemInformatics;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;
import org.ucb.act.ROProjection.Parser;
import smile.neighbor.lsh.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static java.lang.StrictMath.abs;

public class ROUtils {

    /** @author Bryan Hsu **/
    /** shamelessly reusing original ROUtils methods **/

    public static void main(String[] args) throws Exception{
        //a test to demonstrate that we can output mass differences successfully
        Parser parser = new Parser();
        Set<String> ss = parser.RORun("2015_01_16-ROPruner_hchERO_list.txt");
        for(String ro:ss){
            System.out.println(explode(ro).get("Reactants") + "," + explode(ro).get("Products"));
            System.out.println(get_mass_difference(ro));
        }
    }

    public ArrayList<HashMap> pair(String ro){
        return null;
    }

    private static HashMap<String, Molecule[]> explode(String ro) throws Exception {
        RxnMolecule rxn = RxnMolecule.getReaction(MolImporter.importMol(ro));
        Molecule[] substrates = rxn.getReactants();
        Molecule[] products = rxn.getProducts();
        HashMap rxn_species = new HashMap<String, Molecule[]>();
        rxn_species.put("Reactants", substrates);
        rxn_species.put("Products", products);
        return rxn_species;
    }

    private static double get_mass_difference(String ro) throws Exception {
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
