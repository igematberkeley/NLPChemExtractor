package org.ucb.act.ROProjection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ucb.act.ro.ChangeMapper;
import org.ucb.act.ro.ROProjecter;
import org.ucb.act.ro.ROUtils;
import org.ucb.act.ro.SkeletonMapper;
import org.ucb.act.utils.ChemAxonUtils;
import org.ucb.act.ROProjection.Molecule_Projector;

/** @author ArjunChandran **/

public class Mol_Test {
    @Test
    public void test1() throws Exception{
        try {
            ChemAxonUtils.license();
        } catch (Exception err) {
            System.out.print("No License");
        }

        Molecule_Projector projector = new Molecule_Projector();
        Parser parser = new Parser();
        ROUtils utils = new ROUtils();
        Set<String> ROs = parser.RORun("C:/Users/Arjun Chandran/Documents/NLPchem/NLPChemExtractor/Cheminformatics/src/2015_01_16-ROPruner_hchERO_list.txt");
        for (String RO: ROs){
            ChemAxonUtils.savePNGImage(MolImporter.importMol(RO), "TestRO.png");
            HashMap<String, Molecule[]> rxn_break_down = utils.explode(RO);
            Molecule[] reactants = rxn_break_down.get("Reactants");
            int i = 0;
            for (Molecule molecule: reactants){
                String name = "Mol_TestReactent" + Integer.toString(i) + ".png";
                i = i + 1;
                ChemAxonUtils.savePNGImage(molecule, name);
            }
            Molecule[] products = rxn_break_down.get("Products");
            int j = 0;
            for (Molecule molecule: products){
                String name = "Mol_TestProducts" + Integer.toString(j) + ".png";
                j = j + 1;
                ChemAxonUtils.savePNGImage(molecule, name);
            }
            System.out.println("Reactents " + Integer.toString(reactants.length));
            System.out.println("Products " + Integer.toString(products.length));
            ArrayList<Molecule> react = new ArrayList<>();
            ArrayList<Molecule> prod = new ArrayList<>();
            for(Molecule mol: reactants){
                react.add(mol);
            }
            for (Molecule prd: products){
                prod.add(prd);
            }
            System.out.println(RO);
            if (RO.equals("[H][#7]([H])[C:1]([H])([#6:5])[#6:2]=[O:4].[#6:33]-[n+:38]1[c:39][c:40][c:41][c:42]([c:43]1)-[#6:44](-[#8:45])=[O:46].[H][#7]([H])-[c:8]1[n:9][c:10][n:11][c:12]2[n:13][c:14][n:15][c:16]12>>[#6:5]-[#6:1](=[O:46])-[#6:2]=[O:4].[H][#8]-[c:8]1[n:9][c:10][n:11][c:12]2[n:13][c:14][n:15][c:16]12.[H][#7]=[#6:44](-[#8:45])-[#6:42]-1=[#6:43]-[#7:38](-[#6:33])-[#6:39]=[#6:40]-[#6:41]-1[H]")) {
                int p = 0;
            }
            ChemAxonUtils.savePNGImage(MolImporter.importMol(RO), "Test2RO.png");
            Boolean val = projector.MoleculeRxn(RO, react, prod);
            assertTrue(val);
        }
    }

    @Test
    public void test2() throws Exception {
        try {
            ChemAxonUtils.license();
        } catch (Exception err) {
            System.out.print("No License");
        }
        //Converts the below reaction string into an RO
        String reaction = "c1ccccc1CO>>c1ccccc1COC";
        RxnMolecule rxn = RxnMolecule.getReaction(MolImporter.importMol(reaction));
        RxnMolecule result = new SkeletonMapper().map(rxn);
        String RO = ChangeMapper.printOutReaction(result);
        System.out.println("\nresult:\n" + RO);
        ChemAxonUtils.savePNGImage(MolImporter.importMol(RO), "TEST2RO.png");

        //From here we do what test1 does with the specified RO
        Molecule_Projector projector = new Molecule_Projector();
        ROUtils utils = new ROUtils();

        HashMap<String, Molecule[]> rxn_break_down = utils.explode(RO);
        Molecule[] reactants = rxn_break_down.get("Reactants");
        int i = 0;
        for (Molecule molecule: reactants){
            String name = "TEST2Mol_TestReactent" + Integer.toString(i) + ".png";
            i = i + 1;
            ChemAxonUtils.savePNGImage(molecule, name);
        }
        Molecule[] products = rxn_break_down.get("Products");
        int j = 0;
        for (Molecule molecule: products){
            String name = "TEST2Mol_TestProducts" + Integer.toString(j) + ".png";
            j = j + 1;
            ChemAxonUtils.savePNGImage(molecule, name);
        }
        System.out.println("Reactents " + Integer.toString(reactants.length));
        System.out.println("Products " + Integer.toString(products.length));
        ArrayList<Molecule> react = new ArrayList<>();
        ArrayList<Molecule> prod = new ArrayList<>();
        for(Molecule mol: reactants){
            react.add(mol);
        }
        for (Molecule prd: products){
            prod.add(prd);
        }
        System.out.println(RO);
        Boolean val = projector.MoleculeRxn(RO, react, prod);
        assertTrue(val);


    }
}
