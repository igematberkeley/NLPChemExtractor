package org.ucb.act.ROProjection;

import java.util.*;

import chemaxon.struc.Molecule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ucb.act.ro.ROProjecter;
import org.ucb.act.ro.ROUtils;
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
            HashMap<String, Molecule[]> rxn_break_down = utils.explode(RO);
            Molecule[] reactants = rxn_break_down.get("Reactants");
            Molecule[] products = rxn_break_down.get("Products");
            System.out.println("Reactents " + Integer.toString(reactants.length));
            System.out.println("Products " + Integer.toString(products.length));
            ArrayList<Molecule> react = new ArrayList<>();
            ArrayList<Molecule> prod = new ArrayList<>();
            for(Molecule i: reactants){
                react.add(i);
            }
            for (Molecule i: products){
                prod.add(i);
            }
            System.out.println(RO);
            assertTrue(projector.MoleculeRxn(RO, react, prod));
        }
    }
}
