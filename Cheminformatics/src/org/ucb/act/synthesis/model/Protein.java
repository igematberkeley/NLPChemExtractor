package org.ucb.act.synthesis.model;

/**
 *
 * @author J. Christopher Anderson
 */
public class Protein {
    private final String name;
    private final String sequence;
    private final String accession;
    private final String source_organism;

    public Protein(String name, String sequence, String accession, String source_organism) {
        this.name = name;
        this.sequence = sequence;
        this.accession = accession;
        this.source_organism = source_organism;
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return sequence;
    }

    public String getAccession() {
        return accession;
    }

    public String getSource_organism() {
        return source_organism;
    }
}

