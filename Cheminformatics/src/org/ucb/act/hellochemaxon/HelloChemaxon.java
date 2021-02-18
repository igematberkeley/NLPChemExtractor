package org.ucb.act.hellochemaxon;

import chemaxon.formats.MolImporter;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import java.io.File;
import org.ucb.act.synthesis.Synthesizer;
import org.ucb.act.utils.ChemAxonUtils;
import org.ucb.act.utils.FileUtils;

/**
 * Code for Project 1: Hello ChemAxon
 *
 * This is a simple script that shows basic cheminformatics functions of
 * ChemAxon on a single example molecule. Running the code outputs two files.
 * One is an image you can look at. The other text file contains the outputs of
 * inidividual API calls in the script.
 *
 * To complete this assignment, get this code base build, run this class, and
 * fix any build issues until it says (amongst other things) "Success!"
 *
 * Once built, change the chemical in question to benzophenone then upload the
 * output file hellochemaxon_output.txt to bcourses.
 *
 * @author J. Christopher Anderson
 */
public class HelloChemaxon {

    public static void main(String[] args) throws Exception {
        try {
            ChemAxonUtils.license();
        } catch (Exception err) {
            File licensedir = new File("chemaxon_license");
            if(!licensedir.exists()) {
                licensedir.mkdir();
            }
            System.out.println("You need to add the Chemaxon license file on your path. Put it in the newly-generated chemaxon_license directory.");
            System.exit(0);
        }

        StringBuilder sb = new StringBuilder();

        //Import a Molecule from an InChi or a SMILES
        String inchi = "InChI=1S/C28H26N4O3/c1-28-26(34-3)17(29-2)12-20(35-28)31-18-10-6-4-8-14(18)22-23-16(13-30-27(23)33)21-15-9-5-7-11-19(15)32(28)25(21)24(22)31/h4-11,17,20,26,29H,12-13H2,1-3H3,(H,30,33)/t17-,20-,26-,28+/m1/s1";
        Molecule amol = MolImporter.importMol(inchi);

        sb.append(ChemAxonUtils.toSmiles(amol)).append("\n");

        //Access methods of Molecule to inspect the structure
        sb.append(amol.getAtomCount()).append("\n");                //How many atoms in the molecule?
        MolAtom anatom = amol.getAtom(3);                           //Retrieves the atom at index 3
        sb.append(anatom.getAtno()).append("\n");                   //Retrieve the atomic number of an atom
        sb.append(anatom.getHybridizationState()).append("\n");     //sp2, sp3, etc.
        sb.append(anatom.getBondCount()).append("\n");              //Number of bonds attached to an atom
        MolBond itsbond = anatom.getBond(1);                        //Retrieve specific bonds of an atom

        sb.append(amol.getBondCount()).append("\n");                //Returns the total number of bonds in the Molecule
        MolBond abond = amol.getBond(3);                            //Retrieve the bond at index 3
        sb.append(abond.getAtom1().getSymbol()).append("\n");       //Access the atoms attached to a bond
        sb.append(abond.getAtom2().getSymbol()).append("\n");       //And the other end of the bond

        //Test the build for the next rpoject
        try {
            testRunSynthesizer();
        } catch (Exception err) {
            FileUtils.writeFile("", "aksjrk2j54kl254.txt");
            System.out.println("You need to fix your build, you are probably missing files on the path.\nExamine previous error messages to see what is missing.\nTo find your path, locate the file aksjrk2j54kl254.txt which has been generated in your filesystem.");
            System.exit(0);
        }

        //Write out an image of the chemical's structure, and the results of these calls
        FileUtils.writeFile(sb.toString(), "hellochemaxon_output.txt");
        ChemAxonUtils.savePNGImage(amol, "hellochemaxon_chem.png");
        System.out.println("Success!\nlook for hellochemaxon_output.txt");
    }

    private static void testRunSynthesizer() throws Exception {
        Synthesizer synth = new Synthesizer();

        //Populate the chemical and reaction data
        try {
            synth.populateReactions("good_reactions.txt");
        } catch (Exception err) {
            throw new RuntimeException("The file good_reactions.txt isn't on path");
        }
        try {
            synth.populateChemicals("good_chems.txt");
        } catch (Exception err) {
            throw new RuntimeException("The file good_chems.txt isn't on path");
        }

        //Populate the bag of chemicals to consider as shell 0 "natives"
        try {
            synth.populateNatives("minimal_metabolites.txt");
        } catch (Exception err) {
            throw new RuntimeException("The file minimal_metabolites.txt isn't on path");
        }
        try {
            synth.populateNatives("universal_metabolites.txt");
        } catch (Exception err) {
            throw new RuntimeException("The file universal_metabolites.txt isn't on path");
        }

        //Check that the complete reachables list got copied over
        File afile = new File("r-2015-10-06-new-metacyc-with-extra-cofactors.reachables.txt");
        if (!afile.exists()) {
            throw new RuntimeException("The file r-2015-10-06-new-metacyc-with-extra-cofactors.reachables.txt isn't on path");
        }
    }
}
