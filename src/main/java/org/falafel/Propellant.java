package org.falafel;

/**
 * A class representing a propellent package.
 */
public class Propellant extends Material {

    /**
     * Save how much of the material is still left.
     */
    private int quantity;

    /**
     * Create new propellent package with the given attributes.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     * @param supplierName
     *          The name of the supplier
     * @param supplierId
     *          A integer value that (uniquely) identifies the supplier.
     * @param quantity
     *          The quantity of propellent in the propellent package.
     */
    public Propellant(final int identifier, final String supplierName,
                      final int supplierId, final int quantity) {
        super(identifier, supplierName, supplierId);
        this.quantity = quantity;
    }

    /**
     * Return the string representation of the propellent package.
     *
     * @return A string containing properties of this propellent package
     */
    public final String toString() {
       return super.toString() + " -- Quantity: " + quantity;
    }
}
