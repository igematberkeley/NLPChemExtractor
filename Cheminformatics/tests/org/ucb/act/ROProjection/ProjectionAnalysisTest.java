/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ucb.act.utils.ChemAxonUtils;

/**
 *
 * @author jesusdelrio
 */
public class ProjectionAnalysisTest {
    

    /**
     * Test of oneMoleculeRun method, of class ProjectionAnalysis.
     */
    @Test
    
    //Test to determine if the correct reactions are obtained from a set of chemicals
    //Here, the chemicals involved in 3 different reactions are put in the same list to see
    //if oneMoleculeRun can discriminate and find correctly each reaction
    public void testOneMoleculeRun() throws Exception {
        
      
        Parser parser = new Parser();
        ChemAxonUtils.license();
        ProjectionAnalysis projectionAnalysis = new ProjectionAnalysis();

        HashMap<String, String> namesROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        Map<String,String> sentence = new HashMap();
        
        /**
        Chemicals for the first two reactions:
        * 1) butanal + NADH + H+ -> 1-butanol + NAD+ 
        * 2) 1-butanol + NAD+ -> butanal + H+ + NADH
        * 
        * RO used for reaction 1) is [#6:2]-[#6:1]=[O:12]>>[H][#8:12]-[#6:1]([H])-[#6:2] (aldehyde reduction to primary alcohol)
        * RO used for reaction 2) is [H][#8:4]-[#6:3]([H])-[#6:2]>>[#6:2]-[#6:3]=[O:4] (primary alcohol to aldehyde) check: https://www.rhea-db.org/rhea/33199
        */
        sentence.put("butanal","InChI=1S/C4H8O/c1-2-3-4-5/h4H,2-3H2,1H3");
        sentence.put("NAD+","InChI=1S/C21H27N7O14P2/c22-17-12-19(25-7-24-17)28(8-26-12)21-16(32)14(30)11(41-21)6-39-44(36,37)42-43(34,35)38-5-10-13(29)15(31)20(40-10)27-3-1-2-9(4-27)18(23)33/h1-4,7-8,10-11,13-16,20-21,29-32H,5-6H2,(H5-,22,23,24,25,33,34,35,36,37)/t10-,11-,13-,14-,15-,16-,20-,21-/m1/s1");
        sentence.put("1-butanol","InChI=1S/C4H10O/c1-2-3-4-5/h5H,2-4H2,1H3");
        sentence.put("NADH","InChI=1S/C21H29N7O14P2/c22-17-12-19(25-7-24-17)28(8-26-12)21-16(32)14(30)11(41-21)6-39-44(36,37)42-43(34,35)38-5-10-13(29)15(31)20(40-10)27-3-1-2-9(4-27)18(23)33/h1,3-4,7-8,10-11,13-16,20-21,29-32H,2,5-6H2,(H2,23,33)(H,34,35)(H,36,37)(H2,22,24,25)/t10-,11-,13-,14-,15-,16-,20-,21-/m1/s1");
        sentence.put("H+","InChI=1S/p+1");

        /**Chemicals for the last reaction:
         * 3) D-sorbitol 6-phosphate + H2O -> D-sorbitol + phosphate
         * 
         * RO used for reaction 3) is [H][#8]P(=O)([#8][H])[#8:2]-[#6:1]>>[H][#8:2]-[#6:1] (phosphate hydrolisis) check: https://www.rhea-db.org/rhea/24580 
         */ 
        sentence.put("D-sorbitol 6-phosphate","InChI=1S/C6H15O9P/c7-1-3(8)5(10)6(11)4(9)2-15-16(12,13)14/h3-11H,1-2H2,(H2,12,13,14)/t3-,4+,5+,6+/m0/s1");
        sentence.put("H2O","InChI=1S/H2O/h1H2");
        sentence.put("D-sorbitol","InChI=1S/C6H14O6/c7-1-3(9)5(11)6(12)4(10)2-8/h3-12H,1-2H2/t3-,4+,5-,6-/m1/s1");
        sentence.put("phosphate","InChI=1S/H3O4P/c1-5(2,3)4/h(H3,1,2,3,4)/p-3");
       
        //Creation of validated output HashMap, since we are working with hchEROs, cofactors are not included as substrates or products
        HashMap<String[],HashMap<String,Set<String>>> revisedOutput = new HashMap<>();
        
        //Reaction 1
        HashMap<String,Set<String>> roProducts = new HashMap<>();
        Set<String> products = new HashSet<>();
        String[] substrates= {"butanal"};
        products.add("1-butanol"); 
        roProducts.put("[#6:2]-[#6:1]=[O:12]>>[H][#8:12]-[#6:1]([H])-[#6:2]",products);
        revisedOutput.put(substrates,roProducts);
        
        //Reaction 2
        HashMap<String,Set<String>> roProducts1 = new HashMap<>();
        Set<String> products1 = new HashSet<>();
        String[] substrates1= {"1-butanol"};
        products1.add("butanal");
        roProducts1.put("[H][#8:4]-[#6:3]([H])-[#6:2]>>[#6:2]-[#6:3]=[O:4]",products1);
        revisedOutput.put(substrates1,roProducts1);
        
        //Reaction 3
        HashMap<String,Set<String>> roProducts2 = new HashMap<>();
        Set<String> products2 = new HashSet<>();
        String[] substrates2= {"D-sorbitol 6-phosphate"};
        products2.add("D-sorbitol");
        roProducts2.put("[H][#8]P(=O)([#8][H])[#8:2]-[#6:1]>>[H][#8:2]-[#6:1]",products2);
        revisedOutput.put(substrates2,roProducts2);
 
        
        
         
        HashMap<String[],HashMap<String,Set<String>>> outputSingleMolecule = projectionAnalysis.oneMoleculeRun(sentence,namesROs);
        assertTrue(revisedOutput.equals(outputSingleMolecule));

    }

    /**
     * Test of twoMoleculesRun method, of class ProjectionAnalysis.
     */
    @Test
    public void testTwoMoleculesRun() {
 
    }
    
}
