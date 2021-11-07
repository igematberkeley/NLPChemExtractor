package org.ucb.act.ROProjection;

import java.util.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ucb.act.utils.ChemAxonUtils;

/**
 *
 * @author Arjun Chandran
 */ public class Reaction_Generator_test{

     @Test
    public void genTest1() throws Exception{
         Reaction_Generator rxn_gen = new Reaction_Generator();
         String file_path = "";
         HashMap<ArrayList, ArrayList<String[]>> output = rxn_gen.naive_generate(file_path, 4);
         Set<ArrayList> text_loc = output.keySet();
         for (ArrayList<Integer> i: text_loc){
             int x = i.get(1) - i.get(0);
             assertEquals(x, 4);
         }
     }
}