package newChemInformatics;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;
import chemaxon.reaction.Reactor;
import org.ucb.act.ROProjection.Parser;
import smile.neighbor.lsh.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/** @author Cassandra Areff **//

public class Pipeline {

    // since hashmaps cannot have primitives like our doubles for mass, do we just round to integers and check those?

    /** creates a hashmap with all the ro differences **/
    public static HashMap<Integer, String[]> create_ro_hashmap(String[] ros){
        // to be done
        return null;
    }

    /** gets the diff between the substrate and product and then finds
     * ros with same mass diff **/
    public static String[] check_rxn(HashMap<Integer, String[]> ros) {
        // to be done
        return null;
    }

    /** checks if the ros applied to the substrate produces the product **/
    public static boolean viable_rxn(String[] ros) {
        for (String ro: ros) {
            Boolean ro_valid = test_ro(ro, substrate, product);
            if (ro_valid) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String[] ros = new String[]{"example"};
        substrate = InChIUtils.get_inchi_as_mol("somthing");
        product = InChIUtils.get_inchi_as_mol("somthing");
        HashMap<Integer, String[]> ro_database = create_ro_hashmap(ros);
        String[] possible_ros = check_rxn(ro_database);
        System.out.println(viable_rxn(possible_ros));
    }

    private Molecule substrate;
    private Molecule product;
}
