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

        
        String ro = "[H][#8:9]-[c:8]1[c:10][c:3][c:4][c:5][c:6]1>>[H]C([H])([H])[#8:9]-[c:8]1[c:10][c:3][c:4][c:5][c:6]1";

        String[] reactants = new String[1];
        //reactants[0] ="InChI=1S/C15H22N6O5S/c1-27(3-2-7(16)15(24)25)4-8-10(22)11(23)14(26-8)21-6-20-9-12(17)18-5-19-13(9)21/h5-8,10-11,14,22-23H,2-4,16H2,1H3,(H2-,17,18,19,24,25)/p+1/t7-,8+,10+,11+,14+,27?/m0/s1";
        reactants[0] = "InChI=1S/C6H6O/c7-6-4-2-1-3-5-6/h1-5,7H";
        
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
