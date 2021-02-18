package org.ucb.act.synthesis.model;

/**
 * Immutable Class for describing a Chemical
 * 
 * @author J. Christopher Anderson
 */
public class Chemical {
    private final long id;
    private final String inchi;
    private final String smiles;
    private final String name;

    public Chemical(long id, String inchi, String smiles, String name) {
        this.id = id;
        this.inchi = inchi;
        this.smiles = smiles;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getInchi() {
        return inchi;
    }

    public String getSmiles() {
        return smiles;
    }

    public String getName() {
        return name;
    }
}
