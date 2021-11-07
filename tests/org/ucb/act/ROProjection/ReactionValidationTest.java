/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucb.act.ROProjection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jesusdelrio
 */
public class ReactionValidationTest {
    
    /**
     * Test of convertToCSV method, of class ReactionValidation.
     */
    @Test
    public void testConvertToCSV() throws Exception {
        //Not finished
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
        
        //Reaction 5
        HashMap<String,Set<String>> roProducts4 = new HashMap<>();
        Set<String> substrates4 = new HashSet<>();
        Set<String> products4 = new HashSet<>();
        substrates4.add("Methanethiol");
        substrates4.add("S-adenosyl-L-methionine");
        products4.add("dimethyl sulfide");
        products4.add("S-adenosyl-L-homocysteine");
        products4.add("5'-Deoxy-5'-methylthioadenosine");
        products4.add("Methionine");
        roProducts4.put("[H][#16:3]-[#6:2].[#6:5][S+:6]([#6:7])[#6:14]>>[#6:5]-[#16:3]-[#6:2].[#6:7]-[#16:6]-[#6:14]",products4);
        revisedOutput.put(substrates,roProducts);
        
        //ReactionValidation.convertToCSV(outputWithID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
