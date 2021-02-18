package org.ucb.act.composition.model;

import java.util.List;

/**
 *
 * @author J. Christopher Anderson
 */
public class Composition {
    private final String name;
    private final List<Part> parts;
    
    public Composition(String name, List<Part> parts) {
        this.name = name;
        this.parts = parts;
    }

    public String getName() {
        return name;
    }
    
    public List<Part> getParts() {
        return parts;
    }
}

