package org.ucb.act.synthesis;

import java.util.HashSet;
import java.util.Set;
import org.ucb.act.synthesis.model.Chemical;
import org.ucb.act.synthesis.model.Reaction;

/**
 * A Cascade is a Node in the metabolomic hypergraph associated with a 
 * particular chemical, which is the product.
 * 
 * It associates a chemical with the universe of all Reaction that are known
 * to form it.  A Reaction in turn contains links to its substrates, which are
 * in turn associated to their Cascades via the calling Synthesizer.  Thus, the
 * full hypergraph of all Reachable cascades back to native metabolites is
 * pre-computed within the bag of Cascades held by the Synthesizer in the 
 * Map chemicalToCascade.
 * 
 * To meaningfully access the data for one Reachable, you do not pull out an 
 * independent object separate from the rest of the hypergraph.  Instead, you
 * do a simple and direct crawl through the hypergraph as demonstrated in the
 * Synthesizer.java method printoutCascade().
 * 
 * Note that this class is not immutable, and the Reactions are added dynamically
 * in the traceback algorithm.  This function is package-private such that it
 * will behave as immutablke at a public API level.
 * 
 * @author J. Christopher Anderson
 */
public class Cascade {
    private final Chemical product;  //This Cascade represents all routes to this chemical
    private final Set<Reaction> rxnsThatFormPdt= new HashSet<>();

    public Cascade(Chemical product) {
        this.product = product;
    }

    public Chemical getProduct() {
        return product;
    }

    public Set<Reaction> getRxnsThatFormPdt() {
        return rxnsThatFormPdt;
    }
    
    void addReaction(Reaction rxn) {
        rxnsThatFormPdt.add(rxn);
    }
}
