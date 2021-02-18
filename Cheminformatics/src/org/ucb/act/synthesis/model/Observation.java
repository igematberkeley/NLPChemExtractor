package org.ucb.act.synthesis.model;

/**
 *
 * @author J. Christopher Anderson
 */
public class Observation {
    //The pairing of a Reaction and an Enzyme
    private final Reaction reaction;
    private final Enzyme enzyme;
    
    //Whether the combination resulted in a spontaneous reaction
    private final boolean isObserved;
    
    //The experimental basis for the claim
    private final String evidence;
    
    public Observation(Reaction reaction, Enzyme enzyme, boolean isObserved, String evidence) {
        this.reaction = reaction;
        this.enzyme = enzyme;
        this.isObserved = isObserved;
        this.evidence = evidence;
    }

    public Reaction getReaction() {
        return reaction;
    }

    public Enzyme getEnzyme() {
        return enzyme;
    }

    public boolean isObserved() {
        return isObserved;
    }

    public String getEvidence() {
        return evidence;
    }
}

