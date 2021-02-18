package org.ucb.act.targeting;


import chemaxon.formats.MolImporter;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.MolSearch;
import chemaxon.sss.search.MolSearchOptions;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.ucb.act.utils.ChemAxonUtils;
import org.ucb.act.utils.FileUtils;

/**
 * This class does a substructure query against a list of chemicals to pull out
 * indole substructures.
 * 
 * It uses the ChemAxon MolSearch function and SMARTS queries.
 * 
 * Any list of inchis can be passed in as arguments. This is distributed
 * pointing to a 20n list of L2 Reachables derived from an aggregation of
 * data sources.
 * 
 * It can be used to screen the output of Synthesizer.java:
 * "metacyc_L2_reachables.txt"
 * 
 * @author J. christopher Anderson
 */
public class IndoleSearch {
    private final String indole_smarts = "C12=C(C=CN2)C=CC=C1";
    private MolSearch searcher;
   
    /**
     * This initializes ChemAxon and an instance of MolSearch called searcher.
     * This searcher is configured with the SMARTS pattern for indole and 
     * some settings about what to consider a match.
     * 
     * No searches are done in this method, this just configures the search,
     * which will be used many times in the run() method.
     * 
     * @throws Exception 
     */
    public void initiate() throws Exception {
        //Initialize ChemAxon library (requires license file on path)
        ChemAxonUtils.license();
        
        //Construct the MolSearch query
        //From https://docs.chemaxon.com/display/jchembase/Bond+specific+search+options
        searcher = new MolSearch();
        MolSearchOptions searchOptions = new MolSearchOptions(SearchConstants.SUBSTRUCTURE);

        //Set the matching conditions in the SearchOptions
        searchOptions.setVagueBondLevel(SearchConstants.VAGUE_BOND_LEVEL4);
        //searchOptions.setExactBondMatching(false);
        searcher.setSearchOptions(searchOptions);

        // queryMode = true forces string to be imported as SMARTS
        // If SMILES import needed, set queryMode = false.
        MolHandler mh1 = new MolHandler(indole_smarts, true);
        Molecule query = mh1.getMolecule();
        searcher.setQuery(query);
    }

    /**
     * Iterates through a list of inchis and pulls out the subset
     * that contain the specified search pattern (all indoles in this case)
     * 
     * @param chemDataPath
     * @return
     * @throws Exception 
     */
    public Set<String> run(String chemDataPath) throws Exception {
        //Keep a log of failed inchis and matching inchis
        Set<String> failedInchis = new HashSet<>();
        Set<String> matchingInchis = new HashSet<>();
        
        //Parse all product molecules of reactions from Act DB
        String data = FileUtils.readFile(chemDataPath);
        data = data.replaceAll("\"", "");
        String[] lines = data.split("\\r|\\r?\\n");
        for(int i=1; i<lines.length; i++) {
            String[] tabs = lines[i].split("\t");
            String inchi = tabs[2];
            try {
                if(analyze(inchi)) {
                    matchingInchis.add(inchi);
                }
            } catch(Exception err) {
                failedInchis.add(inchi);
            }
        } 
        
        return matchingInchis;
    }
    
    /**
     * Method doing the actual MolSearch
     * 
     * @param inchi
     * @return
     * @throws Exception 
     */
    private boolean analyze(String inchi) throws Exception {
        //Import the target chemical with ChemAxon
        Molecule target  = MolImporter.importMol(inchi);
       
        //Do Molsearch for indole pattern
        searcher.setTarget(target);

        //Search all matching substructures
        //from https://www.chemaxon.com/jchem/doc/dev/java/api/chemaxon/sss/search/MolSearch.html
        int[][] hits = searcher.findAll();
        
        //Return true if the inchi contained the pattern
        if (hits == null) {
            return false;
        }
        return true;
    }
 
    public static void main(String[] args) throws Exception {
        //Do the search using the 20n full L2 Reachables list
        IndoleSearch search = new IndoleSearch();
        search.initiate();
        Set<String> hits = search.run("r-2015-10-06-new-metacyc-with-extra-cofactors.reachables.txt");
//        Set<String> hits = search.run("metacyc_L2_reachables.txt"); //For Synthesizer-derived list
                
        //Make a directory for the output (or clear it)
        File dir = new File("IndoleHits");
        if(dir.exists()) {
            for(File afile : dir.listFiles()) {
                afile.delete();
            }
        } else  {
            dir.mkdir();
        }
        
        //Output images of the chemicals and a file containing the hits
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for(String inchi : hits) {
            sb.append(count).append("\t").append(inchi).append("\n");
            Molecule mol = MolImporter.importMol(inchi);
            ChemAxonUtils.savePNGImage(mol, "IndoleHits/" + count + ".png");
            count++;
        }
        FileUtils.writeFile(sb.toString(), "IndoleHits/inchis.txt");
        
        //Println the number of hits
        System.out.println(hits.size() + " hits found");
    }
}
