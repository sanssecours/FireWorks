package org.falafel;

/**
 * This class represents a piece of wood used to create a rocket.
 */
public class Wood extends Material {

    /**
     * Create a new piece of wood.
     *
     * @param id
     *         The identifier for the rocket
     */
    public Wood(final int id, String supplierName, int supplierId) {
        super(id, supplierName, supplierId, false, 1);
    }

    public String toString() {
        return "Resource ID: " + Integer.toString(super.getID()) + " -- Supplier Name: " + super.getSupplierName() +
                " -- Supplier ID: " + super.getSupplierId();
    }
}
