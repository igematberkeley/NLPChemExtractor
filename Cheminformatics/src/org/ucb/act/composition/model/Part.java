package org.ucb.act.composition.model;

/**
 *
 * @author J. Christopher Anderson
 */
public class Part {
    private final PartType type;
    private final String name;
    private final String sequence;

    public Part(PartType type, String name, String sequence) {
        this.type = type;
        this.name = name;
        this.sequence = sequence;
    }

    public PartType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return sequence;
    }
}


