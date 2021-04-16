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
import org.ucb.act.ro.ROUtils;
/**
 *
 * @author Arjun Chandran
 */

public class ROUtilsTests{
    ///Beta-tests
    @Test
    public void explode_test() throws Exception{
        Parser parser = new Parser();
        ROUtils utils = new ROUtils();
        Set<String> namesROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        HashMap<String, HashMap> explodedROs = new HashMap<>();
        for(String ro: namesROs){
            print(ro);
            HashMap<String, Molecule[]> rxn = utils.explode(ro);
            explodedROs.add(ro, rxn);

        }
        print(explodedROs);
    }

    @Test
    public void get_mass_difference_test() throws Exception{
        ROUtils utils = new ROUtils();
        Parser parser = new Parser()
        Set<String> namesROs = parser.RORun("./2015_01_16-ROPruner_hchERO_list.txt");
        for(String ro: namesROs){
            print(ro);
            double mass_diff = utils.get_mass_difference(ro)
            explodedROs.add(ro, mass_diff);

        }
        print(explodedROs);
    }
}