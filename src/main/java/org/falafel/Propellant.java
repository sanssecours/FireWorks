package org.falafel;

import org.mozartspaces.capi3.Queryable;

/**
 * A class representing a propellent package.
 */
@Queryable
public class Propellant extends Material {

    /**
     * Save how much gram a full package of the propellant charge has.
     */
    public static final int FULL = 500;
    /**
     * Save how much gram a full package of the propellant charge has.
     */
    public static final String CLOSED = "closed";
    /**
     * Save how much gram a full package of the propellant charge has.
     */
    public static final String OPENED = "opened";

    /**
     * Save how much of the material is still left.
     */
    private int quantity;
    /**
     * Save if the package is opened.
     */
    private String status;

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
    public Propellant(final Integer identifier, final String supplierName,
                      final Integer supplierId, String packageStatus) {
        super(identifier, supplierName, supplierId);
        this.quantity = FULL;
        this.status = packageStatus;
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
     * Return the quantity inside the propellant package.
     *
     * @param takenQuantity
     *          Quantity taken of the Package
     */
    public final void setQuantity(final int takenQuantity) {
        quantity = takenQuantity;
        status = OPENED;
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
