package org.falafel;

import java.io.Serializable;

/**
 * This class stores information about a material such as wood or propellant.
 */
public class Material implements Serializable {

    /** Save an identifier for the material. */
    private final int id;
    /** Save the name of the supplier. */
    private final String supplierName;
    /** Save the id of the supplier. */
    private final int supplierId;
    /** Save if the material is defect. */
    private final boolean defect;
    /** Save how much of the material is still left. */
    private int quantity;

    /**
     * Create a new material with the given identifier.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     *
     */
    public Material(int identifier, String supplierName, int supplierIdd, boolean defect, int quantity) {
        id = identifier;
        this.supplierName = supplierName;
        this.supplierId = supplierIdd;
        this.defect = defect;
        this.quantity = quantity;
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
     * Get the identifier of the material.
     *
     * @return The (unique) supplier name of the material
     *
     */
    public final String getSupplierName() {
        return supplierName;
    }

    /**
     * Get the identifier of the material.
     *
     * @return The (unique) supplier ID of the material
     *
     */
    public final int getSupplierId() {
        return supplierId;
    }

    /**
     * Represent the material as a string.
     *
     * @return A string representing the properties of the material
     */
    public String toString() {
        return "Resource ID: " + Integer.toString(id) + " -- Supplier Name: " + supplierName +
                " -- Supplier ID: " + supplierId + " -- Defect: " + defect  + " -- Quantity: " + quantity;
    }
}
