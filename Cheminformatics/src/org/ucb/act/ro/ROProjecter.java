package org.ucb.act.ro;

import chemaxon.formats.MolExporter;
import chemaxon.reaction.Reactor;
import chemaxon.struc.RxnMolecule;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import java.util.HashSet;
import java.util.Set;
import org.ucb.act.utils.ChemAxonUtils;

/**
 * Uses ChemAxon to do an RO projection
 *
 * Created by jca20n on 10/31/15.
 */
public class ROProjecter {

    public static void main(String[] args) throws Exception {
        
        ChemAxonUtils.license();

        String ro = "[#6:2]-[#6:1]=[O:7]>>[#6:2]-[#6:1](-[#8])=[O:7]";

        String[] reactants = new String[1];
        reactants[0] ="InChI=1S/C6H9N3O/c7-5(3-10)1-6-2-8-4-9-6/h2-5H,1,7H2,(H,8,9)/t5-/m0/s1";
        //reactants[1] = "CC(=O)OCCN1CCN(CCO)CC1";

        Set<String> pdts = new ROProjecter().project(ro, reactants);
        for (String inchi : pdts) {
            System.out.println("\t" + inchi);
        }
    }

    /**
     * Calculates the products of applying a reaction operator (expressed as a
     * SMARTS reaction) on an array of test substrates (expressed as SMILES)
     *
     * Returns a List of product sets. The size of the List corresponds to the
     * number of reactive sites on the test substrates. Each Set entry in the
     * List is the inchis of the products of that projection.
     *
     * @param ro
     * @param reactantSmiles
     * @return
     * @throws Exception
     */
    public Set<String> project(String ro, String[] reactantSmiles) throws Exception {
        Molecule[] reactants = new Molecule[reactantSmiles.length];
        for (int i = 0; i < reactantSmiles.length; i++) {
            String asmile = reactantSmiles[i];
            Molecule reactant = MolImporter.importMol(asmile);
            reactant.aromatize();
            reactants[i] = reactant;
        }

        return project(ro, reactants);
    }

    /**
     * Returns the Set of all products that would result by application of a
     * supplied Reaction Operator (ro) upon a supplied array of substrates
     *
     * @param ro
     * @param substrates
     * @return
     * @throws Exception
     */
    public Set<String> project(String ro, Molecule[] substrates) throws Exception {
        //Read in the Reaction Operator (ro) into ChemAxon
        RxnMolecule reaction = RxnMolecule.getReaction(MolImporter.importMol(ro));

        //Instantiate and popoulate the Reactor
        Reactor reactor = new Reactor();
        reactor.setReaction(reaction);
        reactor.setReactants(substrates);

        //Run the Reactor and collect the inchis of predicted products
        Set<String> inchisOut = new HashSet<>();

        Molecule[] result;
        while ((result = reactor.react()) != null) {
            for (Molecule product : result) {
                String inchi = MolExporter.exportToFormat(product, "inchi:AuxNone,Woff,SAbs");
                inchisOut.add(inchi);
            }
        }

        return inchisOut;
    }
}
