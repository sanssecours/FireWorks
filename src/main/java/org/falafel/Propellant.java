package org.falafel;

/**
 * A class representing a propellent package.
 */
public class Propellant extends Material {

    /**
     * Save how much gram a full package of the propellant charge has.
     */
    public static final int FULL = 500;

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
     */
    public Propellant(final int identifier, final String supplierName,
                      final int supplierId) {
        super(identifier, supplierName, supplierId);
        this.quantity = FULL;
    }

    /**
     * Return the quantity inside the propellant package.
     *
     * @return containing the current quantity of the propellant package.
     */
    public final int getQuantity() {
        return quantity;
    }

    /**
     * Return the string representation of the propellant package.
     *
     * @return A string containing properties of this propellant package
     */
    public final String toString() {
       return super.toString() + " -- Quantity: " + quantity;
    }
}
