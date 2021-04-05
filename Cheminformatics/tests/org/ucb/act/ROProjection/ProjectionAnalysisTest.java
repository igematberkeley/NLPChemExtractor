/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
   *Test to determine if the correct reactions are obtained from a set of chemicals
   *Here, the chemicals involved in 4 different reactions are put in the same list to see
   *if oneMoleculeRun can discriminate and find correctly each reaction 
   */
    @Test
    public void testOneMoleculeRun() throws Exception {
        
      
        Parser parser = new Parser();
        ChemAxonUtils.license();
        ProjectionAnalysis projectionAnalysis = new ProjectionAnalysis();

        Set<String> namesROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        Map<String,String> sentence = new HashMap();
        
        /**
        Chemicals for the first two reactions:
        * 1) butanal + NADH + H+ -> 1-butanol + NAD+ 
        * 2) 1-butanol + NAD+ -> butanal + H+ + NADH
        * 
        * RO used for reaction 1) is [#6:2]-[#6:1]=[O:12]>>[H][#8:12]-[#6:1]([H])-[#6:2] (aldehyde reduction to primary alcohol)
        * ROs used for reaction 2) are [H][#8:4]-[#6:3]([H])-[#6:2]>>[#6:2]-[#6:3]=[O:4] (primary alcohol to aldehyde) 
        * and [H][#6:2]-[#8:1][H]>>[#6:2]=[O:1] (alcohol oxidation to aldehyde) check: https://www.rhea-db.org/rhea/33199
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

        /**Chemicals for the last reaction:
         * 4) phenol + S-adenosyl-L-methionine = anisole + H+ + S-adenosyl-L-homocysteine
         * 
         * 2 RO can be projected for reaction 4) [H][#8:12]-[#6:1]>>[H]C([H])([H])[#8:12]-[#6:1] (alcohol_methylation_to_ether) 
         * and [H][#8:9]-[c:8]1[c:10][c:3][c:4][c:5][c:6]1>>[H]C([H])([H])[#8:9]-[c:8]1[c:10][c:3][c:4][c:5][c:6]1 (phenol_methylation_to_ether) check: https://www.rhea-db.org/rhea/14809
         *Important note!!! RO projection does not care about S-adenosyl-L-methionine being present, maybe we could focus more on 1 subtrate porjection as it could validate 
         * 2 substrate reactions?
         */ 
        sentence.put("phenol","InChI=1S/C6H6O/c7-6-4-2-1-3-5-6/h1-5,7H");
        sentence.put("anisole","InChI=1S/C7H8O/c1-8-7-5-3-2-4-6-7/h2-6H,1H3");
        
        
        //Added two random chemicals that are very unlikely to react with the above substrates and products. Just to check any false positive reactions
        sentence.put("L-arginine","InChI=1S/C6H14N4O2/c7-4(5(11)12)2-1-3-10-6(8)9/h4H,1-3,7H2,(H,11,12)(H4,8,9,10)/t4-/m0/s1");
        sentence.put("L-lysine","InChI=1S/C6H14N2O2/c7-4-2-1-3-5(8)6(9)10/h5H,1-4,7-8H2,(H,9,10)/t5-/m0/s1");
        //Creation of validated output HashMap, since we are working with hchEROs, cofactors are not included as substrates or products
        HashMap<Set<String>,HashMap<String,Set<String>>> revisedOutput = new HashMap<>();
        
        //Reaction 1
        HashMap<String,Set<String>> roProducts = new HashMap<>();
        Set<String> substrates = new HashSet<>();
        Set<String> products = new HashSet<>();
        substrates.add("butanal");
        products.add("1-butanol");
        roProducts.put("[#6:2]-[#6:1]=[O:12]>>[H][#8:12]-[#6:1]([H])-[#6:2]",products);
        revisedOutput.put(substrates,roProducts);
        
        //Reaction 2
        HashMap<String,Set<String>> roProducts1 = new HashMap<>();
        Set<String> substrates1 = new HashSet<>();
        Set<String> products1 = new HashSet<>();
        substrates1.add("1-butanol");
        products1.add("butanal");
        roProducts1.put("[H][#8:4]-[#6:3]([H])-[#6:2]>>[#6:2]-[#6:3]=[O:4]",products1);
        roProducts1.put("[H][#6:2]-[#8:1][H]>>[#6:2]=[O:1]",products1);
        revisedOutput.put(substrates1,roProducts1);
        
        //Reaction 3
        HashMap<String,Set<String>> roProducts2 = new HashMap<>();
        Set<String> substrates2 = new HashSet<>();
        Set<String> products2 = new HashSet<>();
        substrates2.add("D-sorbitol 6-phosphate");
        products2.add("D-sorbitol");
        roProducts2.put("[H][#8]P(=O)([#8][H])[#8:2]-[#6:1]>>[H][#8:2]-[#6:1]",products2);
        revisedOutput.put(substrates2,roProducts2);
        
        //Reaction 4
        HashMap<String,Set<String>> roProducts3 = new HashMap<>();
        Set<String> substrates3 = new HashSet<>();
        Set<String> products3 = new HashSet<>();
        substrates3.add("phenol");
        products3.add("anisole");
        roProducts3.put("[H][#8:12]-[#6:1]>>[H]C([H])([H])[#8:12]-[#6:1]",products3);
        roProducts3.put("[H][#8:9]-[c:8]1[c:10][c:3][c:4][c:5][c:6]1>>[H]C([H])([H])[#8:9]-[c:8]1[c:10][c:3][c:4][c:5][c:6]1",products3);
        revisedOutput.put(substrates3,roProducts3);
        
        
        HashMap<Set<String>,HashMap<String,Set<String>>> outputSingleMolecule = projectionAnalysis.oneMoleculeRun(sentence,namesROs);
        assertTrue(revisedOutput.equals(outputSingleMolecule));
        
        

    }

    /**
     * Test of twoMoleculesRun method, of class ProjectionAnalysis.
     */
    @Test
    public void testTwoMoleculesRun() throws Exception {
        
        
        Parser parser = new Parser();
        ChemAxonUtils.license();
        ProjectionAnalysis projectionAnalysis = new ProjectionAnalysis();

        Set<String> namesROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        Map<String,String> sentence = new HashMap();
        
        /**
         * Chemicals for reaction:
         * Methanethiol + S-adenosyl-L-methionine -> H+ + dimethyl sulfide + S-adenosyl-L-homocysteine
         * 
         * RO for reaction is [H][#16:3]-[#6:2].[#6:5][S+:6]([#6:7])[#6:14]>>[#6:5]-[#16:3]-[#6:2].[#6:7]-[#16:6]-[#6:14] (thiol_and_sulfonium_transmethylation) check:https://www.rhea-db.org/rhea/50428 
         */
        
        //Tricky, it is detecting methylation by both, like a reversible reaction, is that possible? again, no secondary molecule to validate the obtention of a methyl group
        sentence.put("Methanethiol","InChI=1S/CH4S/c1-2/h2H,1H3");
        sentence.put("S-adenosyl-L-methionine","InChI=1S/C15H22N6O5S/c1-27(3-2-7(16)15(24)25)4-8-10(22)11(23)14(26-8)21-6-20-9-12(17)18-5-19-13(9)21/h5-8,10-11,14,22-23H,2-4,16H2,1H3,(H2-,17,18,19,24,25)/p+1/t7-,8+,10+,11+,14+,27?/m0/s1");
        sentence.put("dimethyl sulfide","InChI=1S/C2H6S/c1-3-2/h1-2H3");
        sentence.put("S-adenosyl-L-homocysteine","InChI=1S/C14H20N6O5S/c15-6(14(23)24)1-2-26-3-7-9(21)10(22)13(25-7)20-5-19-8-11(16)17-4-18-12(8)20/h4-7,9-10,13,21-22H,1-3,15H2,(H,23,24)(H2,16,17,18)/t6-,7+,9+,10+,13+/m0/s1");
        //This last two products are inexplicably part of the RO projection
        sentence.put("5'-Deoxy-5'-methylthioadenosine","InChI=1S/C11H15N5O3S/c1-20-2-5-7(17)8(18)11(19-5)16-4-15-6-9(12)13-3-14-10(6)16/h3-5,7-8,11,17-18H,2H2,1H3,(H2,12,13,14)/t5-,7-,8-,11-/m1/s1");
        sentence.put("Methionine","InChI=1S/C5H11NO2S/c1-9-3-2-4(6)5(7)8/h4H,2-3,6H2,1H3,(H,7,8)/t4-/m0/s1");
        
        HashMap<Set<String>,HashMap<String,Set<String>>> revisedOutput = new HashMap<>();
        //Revised output creation
        HashMap<String,Set<String>> roProducts = new HashMap<>();
        Set<String> substrates = new HashSet<>();
        Set<String> products = new HashSet<>();
        substrates.add("Methanethiol");
        substrates.add("S-adenosyl-L-methionine");
        products.add("dimethyl sulfide");
        products.add("S-adenosyl-L-homocysteine");
        products.add("5'-Deoxy-5'-methylthioadenosine");
        products.add("Methionine");
        roProducts.put("[H][#16:3]-[#6:2].[#6:5][S+:6]([#6:7])[#6:14]>>[#6:5]-[#16:3]-[#6:2].[#6:7]-[#16:6]-[#6:14]",products);
        revisedOutput.put(substrates,roProducts);
        
        
        //Only when S-adenosyl-L-homocysteine alone is projected works, wihtout considering how the methyl group was added to the mixture (in this case from glycine betaine
        //HashMap<Set<String>,HashMap<String,Set<String>>> outputOneMolecule = projectionAnalysis.oneMoleculeRun(sentence,namesROs);
        HashMap<Set<String>,HashMap<String,Set<String>>> outputTwoMolecule = projectionAnalysis.twoMoleculesRun(sentence,namesROs);
         assertTrue(revisedOutput.equals(outputTwoMolecule));
        
    }
    
}
