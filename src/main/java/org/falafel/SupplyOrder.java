package org.falafel;

/**
 *  Class to save the orders.
 */
public class SupplyOrder {

    /** Defines the name of the supplier. */
    private String supplierName;
    /** Defines the type of the material the supplier provides. */
    private String type;
    /** Defines how much material the supplier provides. */
    private String quantityStringProperty;
    /** Defines how much mof the supplied material is broken. */
    private String qualityStringProperty;
    /** Defines which color the supplied material is. */
    private String colorStringProperty;

    /** The quantity of the current material. */
    private Integer quantity;
    /** The quality of the current material. */
    private Integer quality;
    /** The color of the current material. */
    private EffectColor color;

    /**
     * Create a new order with the specified attributes.
     *
     * @param name
     *          The name of the supplier
     * @param type
     *          The type of the supplied material
     * @param color
     *          The color of the supplied material
     * @param quantity
     *          The quantity of the supplied material
     * @param quality
     *          The quality of the supplied material
     */
    public SupplyOrder(final String name, final String type,
                       final EffectColor color, final int quantity,
                       final int quality) {
        supplierName = name;
        this.type = type;
        this.quantity = quantity;
        this.quantityStringProperty = Integer.toString(quantity);
        this.quality = quality;
        this.qualityStringProperty = Integer.toString(quality);
        this.color = color;
        this.colorStringProperty = color.toString();
    }

    //CHECKSTYLE:OFF
    /** Create a new predefined order. */
    public SupplyOrder() {
        this("Name", "Wood", EffectColor.Blue, 1, 100);
    }
    //CHECKSTYLE:ON

    /**
     * Get the name of the supplier.
     *
     * @return A string containing the name of the supplier.
     */
    public final String getSupplierName() {
        return supplierName;
    }

    /**
     * Set the name of the supplier.
     *
     * @param name
     *          The name which should be used for the supplier.
     */
    public final void setSupplierName(final String name) {
        this.supplierName = name;
    }


    /**
     * Get the material for the order.
     *
     * @return A string containing the type of the supplied material
     */
    public final String getType() {
        return type;
    }

    /**
     * Set the material for the order.
     *
     * @param type
     *          The type which should be used for the order
     */
    public final void setType(final String type) {
        this.type = type;
    }


    /**
     * Get the quantity of the material in this order.
     *
     * @return A integer representing the quantity of material for this
     *         order.
     */
    public final int getQuantity() {
        return quantity;
    }

    /**
     * Get the quality of the material.
     *
     * @return The quality of the current material
     */
    public final int getQuality() {
        return quality;
    }

    /**
     * Get the effect color of the order.
     *
     * @return color of the order
     */
    public final EffectColor getColor() {
        return color;
    }

    /**
     * Set the effect color of the order.
     *
     * @param newColor the new color of the order
     */
    public final void setColor(final String newColor) {
        colorStringProperty = newColor;
        if (newColor.equals(EffectColor.Blue.toString())) {
            color = EffectColor.Blue;
        } else if (newColor.equals(EffectColor.Green.toString())) {
            color = EffectColor.Green;
        } else {
            color = EffectColor.Red;
        }
    }
    /**
     * Get the string representation for the order.
     *
     * @return A string representing the order
     */
    public final String toString() {
        return "Order: Supplier: " +  supplierName + " -- Type: "
                + type + " -- Color: " + getColor()
                + " -- Quantity: " + getQuantity()
                + " -- Quality: " + getQuality();
    }
}
