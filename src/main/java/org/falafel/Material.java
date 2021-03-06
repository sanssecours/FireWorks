package org.falafel;

import java.io.Serializable;

/**
 * This class stores information about a material such as wood or propellant.
 */
public class Material implements Serializable {

    /** Save an identifier for the material. */
    private Integer id;
    /** Save the name of the supplier. */
    private final String supplierName;
    /** Save the id of the supplier. */
    private final Integer supplierId;

    /**
     * Create a new material with the given attributes.
     *
     * @param identifier
     *          A integer value that identifies this material.
     * @param supplierName
     *          The name of the supplier
     * @param supplierId
     *          A integer value that (uniquely) identifies the supplier.
     *
     */
    public Material(final Integer identifier, final String supplierName,
                    final Integer supplierId) {
        id = identifier;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
    }

    /**
     * Get the identifier of the material.
     *
     * @return The (unique) identifier of the material
     *
     */
    @SuppressWarnings("unused")
    public final int getID() {
        return id;
    }

    /**
     * Get the identifier of the material.
     *
     * @param newId
     *          The (unique) identifier of the material
     */
    public final void setID(final int newId) {
        id = newId;
    }

    /**
     * Get the identifier of the material.
     *
     * @return The (unique) supplier name of the material
     *
     */
    @SuppressWarnings("unused")
    public final String getSupplierName() {
        return supplierName;
    }

    /**
     * Get the identifier of the material.
     *
     * @return The (unique) supplier ID of the material
     *
     */
    @SuppressWarnings("unused")
    public final int getSupplierId() {
        return supplierId;
    }

    //CHECKSTYLE:OFF
    /**
     * Represent the material as a string.
     *
     * @return A string representing the properties of the material
     */
    public String toString() {
        return "ID: " + id + " -- Supplier Name: "
                + supplierName + " -- Supplier ID: " + supplierId;
    }
    //CHECKSTYLE:ON
}
