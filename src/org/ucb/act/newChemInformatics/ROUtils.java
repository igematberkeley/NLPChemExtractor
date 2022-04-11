package newChemInformatics;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.reaction.ReactionException;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;
import chemaxon.reaction.Reactor;
import org.ucb.act.ROProjection.Parser;
import smile.neighbor.lsh.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static java.lang.StrictMath.abs;

public class ROUtils {

    /** @author Bryan Hsu and Cassandra Areff **/
    /** shamelessly reusing original ROUtils methods **/

    /** testing the functions in this class **/
    public static void main(String[] args) throws Exception{
        //a test to demonstrate that we can output mass differences successfully
        Parser parser = new Parser();
        Set<String> ss = parser.RORun("2015_01_16-ROPruner_hchERO_list.txt");
        for(String ro:ss){
            System.out.println(explode(ro).get("Reactants") + "," + explode(ro).get("Products"));
            System.out.println(get_mass_difference(ro));
        }
    }

    /** in progress, may discard, but intended to pair sub and pro of ro if implemented **/
    public ArrayList<HashMap> pair(String ro){
        return null;
    }

    /** expands the ro to get the substrates and products **/
    public static HashMap<String, Molecule[]> explode(String ro) throws Exception {
        RxnMolecule rxn = RxnMolecule.getReaction(MolImporter.importMol(ro));
        Molecule[] substrates = rxn.getReactants();
        Molecule[] products = rxn.getProducts();
        HashMap rxn_species = new HashMap<String, Molecule[]>();
        rxn_species.put("Reactants", substrates);
        rxn_species.put("Products", products);
        return rxn_species;
    }

    /** gets the mass difference for an ro **/
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

    /** for a specific ro it applies the ro to the substrate and sees if the
     * product matches the possible products **/
    public static boolean test_ro(String ro, Molecule substrate, Molecule product) throws MolFormatException {

        RxnMolecule reaction = RxnMolecule.getReaction(MolImporter.importMol(ro));
        Molecule[] subArray = new Molecule[]{substrate};

        Reactor reactor = new Reactor();
        try {
            reactor.setReaction(reaction);
        } catch (ReactionException e) {
            e.printStackTrace();
        }
        try {
            reactor.setReactants(subArray);
        } catch (ReactionException e) {
            e.printStackTrace();
        }

        Molecule[] products = new Molecule[0];
        try {
            products = reactor.react();
        } catch (ReactionException e) {
            e.printStackTrace();
        }
        if (products != null) {
            for (Molecule pro: products) {
                if (InChIUtils.get_mol_as_inchi(product) == InChIUtils.get_mol_as_inchi(pro)) {
                    return true;
                }
            }
        }
        return false;
    }

}
