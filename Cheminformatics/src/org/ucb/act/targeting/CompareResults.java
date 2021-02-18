package org.ucb.act.targeting;

import java.util.HashSet;
import java.util.Set;

/**
 * A demonstration of how to compare sets of inchis
 * 
 * @author J. Christopher Anderson
 */
public class CompareResults {
    public static void main(String[] args) throws Exception {
        IndoleSearch searcher = new IndoleSearch();
        searcher.initiate();
        
        //Run the indole query on both Reachables lists
        Set<String> twentynInchis = searcher.run("r-2015-10-06-new-metacyc-with-extra-cofactors.reachables.txt");
        Set<String> ucbInchis = searcher.run("metacyc_L2_reachables.txt");
        
        //Which inchis are only in the new ucb set?
        Set<String> temp1 = new HashSet<>();
        temp1.addAll(ucbInchis);
        temp1.removeAll(twentynInchis);
        System.out.println(temp1.size() + " indole inchis are unique to the UCB set");
        
        //Which inchis are only in the 20n set?
        Set<String> temp2 = new HashSet<>();
        temp2.addAll(twentynInchis);
        temp2.removeAll(ucbInchis);
        System.out.println(temp2.size() + " indole inchis are unique to the 20n set");
    }
}
