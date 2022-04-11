package newChemInformatics;

import chemaxon.formats.MolFormatException;
import chemaxon.jep.function.In;
import chemaxon.struc.Molecule;

import java.util.*;

import static newChemInformatics.ROUtils.test_ro;

/** @author Cassandra Areff **/

public class Pipeline {

    // since hashmaps cannot have primitives like our doubles for mass, do we just round to integers and check those?

    /** creates a hashmap with all the ro differences times 1000 **/
    public static HashMap<Integer, String[]> create_ro_hashmap(String[] ros){
        HashMap<Integer, String[]> ro_hash = new HashMap<Integer, String[]>();
        for (String ro: ros) {
            try {
                HashMap<String, Molecule[]> exploded = ROUtils.explode(ro);
                Molecule[] reactants = exploded.get("Reactants");
                Molecule[] products = exploded.get("Products");
                // get rid of cofactors here in the future
                Integer mass_difference = InChIUtils.get_mass_difference(reactants, products);
                try {
                    List<String> new_ros = Arrays.asList(ro_hash.get(mass_difference));
                    new_ros.add(ro);
                    String[] new_ro_array = new_ros.toArray(new String[new_ros.size()]);
                    ro_hash.put(mass_difference, new_ro_array);
                } catch (Exception e) {
                    String[] mass_ros = new String[]{ro};
                    ro_hash.put(mass_difference, mass_ros);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ro_hash;
    }

    /** gets the diff between the substrate and product and then finds
     * ros with same mass diff **/
    public static String[] check_rxn(HashMap<Integer, String[]> ros) {
        // get rid of cofactors in the mass difference!!
        Integer mass_difference = InChIUtils.get_mass_difference(substrate, product);
        try {
            String[] ro_options = ros.get(mass_difference);
            return ro_options;
        } catch (Exception e) {
            System.out.println("mass not for an ro");
        }
        return null;
    }

    /** checks if the ros applied to the substrate produces the product **/
    public static boolean viable_rxn(String[] ros) throws MolFormatException {
        // unsure if this works oop
        if (substrate.length == 1 && product.length == 1) {
            for (String ro : ros) {
                Boolean ro_valid = test_ro(ro, substrate[0], product[0]);
                if (ro_valid) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("create hash map testing");
        String[] roInchi = new String[]{"[#6:1]-[#6:7]=O>>[H][#7]([H])-[#6:7]([H])-[#6:1]", "[#6:2]-[#16:3]-[#6:4]>>[H]C([H])([H])[S+:3]([#6:2])[#6:4]"};
        HashMap<Integer, String[]> masses = create_ro_hashmap(roInchi);
        System.out.println(masses.keySet());
        System.out.println(masses.get(89084)[0]);
        String ro = "[#6:1]-[#6:7]=O>>[H][#7]([H])-[#6:7]([H])-[#6:1]";
        try {
            HashMap<String, Molecule[]> exploded = ROUtils.explode(ro);
            substrate = exploded.get("Reactants");
            product =  exploded.get("Products");
            System.out.println(check_rxn(masses).length);
            //System.out.println(viable_rxn(check_rxn(masses))); need one to one to test
        } catch (Exception e) {
            System.out.println("invalid ro lol");
        }
    }

    private static Molecule[] substrate;
    private static Molecule[] product;
}
