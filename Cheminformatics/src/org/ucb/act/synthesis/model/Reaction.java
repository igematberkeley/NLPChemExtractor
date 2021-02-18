package org.ucb.act.synthesis.model;

import java.util.Set;

/**
 * Immutable class describing a chemical reaction
 * 
 * @author J. Christopher Anderson
 */
public class Reaction {
    private final Long id;
    private final Set<Chemical> substrates;
    private final Set<Chemical> products;

    public Reaction(Long id, Set<Chemical> substrates, Set<Chemical> products) {
        this.id = id;
        this.substrates = substrates;
        this.products = products;
    }

    public Long getId() {
        return id;
    }

    public Set<Chemical> getSubstrates() {
        return substrates;
    }

    public Set<Chemical> getProducts() {
        return products;
    }
}
