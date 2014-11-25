package org.falafel;

/**
 * A class representing an effect charge.
 */
public class Effect extends Material {

    /** Save if this effect is defect. */
    private boolean defect;

    /**
     * Create new propellent package with the given attributes.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     * @param supplierName
     *          The name of the supplier
     * @param supplierId
     *          A integer value that (uniquely) identifies the supplier.
     * @param defect
     *          A boolean value specifying if this effect is defect or not
     */
    public Effect(final int identifier, final String supplierName,
                      final int supplierId, final boolean defect) {
        super(identifier, supplierName, supplierId);
        this.defect = defect;
    }

    /**
     * Return the string representation of the propellent package.
     *
     * @return A string containing properties of this propellent package
     */
    public final String toString() {
        return "Effect: " + super.toString() + " -- Defect: " + defect;
    }
}
