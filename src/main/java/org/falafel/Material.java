package org.falafel;

import java.io.Serializable;

/**
 * This class stores information about a material such as wood or propellant.
 */
public class Material implements Serializable {

    /** Save an identifier for the material. */
    private final int id;

    /**
     * Create a new material with the given identifier.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     *
     */
    public Material(final int identifier) {
        id = identifier;
    }

    /**
     * Get the identifier of the material.
     *
     * @return The (unique) identifier of the material
     *
     */
    public final int getID() {
        return id;
    }

    /**
     * Represent the material as a string.
     *
     * @return A string representing the properties of the material
     */
    public final String toString() {
        return Integer.toString(getID());
    }
}
