package newChemInformatics;

import chemaxon.formats.MolFormatException;
import chemaxon.jep.function.In;
import chemaxon.standardizer.Standardizer;
import chemaxon.struc.Molecule;
import com.sun.xml.bind.api.impl.NameConverter;
import org.ucb.act.utils.ChemAxonUtils;

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
                standardize(reactants);
                standardize(products);
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
        // here to make sure molecules are normalized/neutralized properly
        System.out.println(mass_difference);
        try {
            String[] ro_options = ros.get(mass_difference);
            return ro_options;
        } catch (Exception e) {
            System.out.println("mass not for an ro");
        }
        return null;
    }

    public static void standardize(Molecule[] molArray){
        Standardizer aromatizer = new Standardizer("aromatize");
        Standardizer neutralizer = new Standardizer("neutralize");
        Standardizer tautomerizer = new Standardizer("tautomerize");
        Standardizer addExplicitH = new Standardizer("addexplicitH");
        for(int i = 0; i< molArray.length; i++){
            aromatizer.standardize(molArray[i]);
            neutralizer.standardize(molArray[i]);
            tautomerizer.standardize(molArray[i]);
            addExplicitH.standardize(molArray[i]);
        }
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


    public static void main(String[] args) throws Exception {
        ChemAxonUtils.license();
        System.out.println("create hash map testing");
        String[] roInchi = new String[]{"[#6:1]-[#6:7]=O>>[H][#7]([H])-[#6:7]([H])-[#6:1]", "[#6:2]-[#16:3]-[#6:4]>>[H]C([H])([H])[S+:3]([#6:2])[#6:4]", "[#6:2]-[#6:1]=[O:12]>>[H][#8:12]-[#6:1]([H])-[#6:2]"};
        HashMap<Integer, String[]> masses = create_ro_hashmap(roInchi);
        System.out.println(masses.keySet());
        System.out.println(masses.get(90068)[0]);
        String ro = "[#6:1]-[#6:7]=O>>[H][#7]([H])-[#6:7]([H])-[#6:1]";
        substrate = new Molecule[]{InChIUtils.get_inchi_as_mol("InChI=1S/C4H8O/c1-2-3-4-5/h4H,2-3H2,1H3")};
        product =  new Molecule[]{InChIUtils.get_inchi_as_mol("InChI=1S/C4H10O/c1-2-3-4-5/h5H,2-4H2,1H3")};
        // can do once we normalize/neutralize the molecules
        System.out.println(check_rxn(masses).length);
        System.out.println(check_rxn(masses)[0]);
        try {
            System.out.println(viable_rxn(check_rxn(masses))); //need one to one to test
        } catch (MolFormatException e) {
            e.printStackTrace();
        }
    }

    private static Molecule[] substrate;
    private static Molecule[] product;
}
