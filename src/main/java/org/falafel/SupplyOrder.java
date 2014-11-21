package org.falafel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *  Class to save the orders.
 */
public class SupplyOrder {

    private final StringProperty supplierName;
    private final StringProperty type;
    private final StringProperty quantity;
    private final StringProperty quality;

    public SupplyOrder(String name, String type, int quantity, int quality) {
        supplierName = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.quantity = new SimpleStringProperty(Integer.toString(quantity));
        this.quality = new SimpleStringProperty(Integer.toString(quality));
    }

    public String getSupplierName() {
        return supplierName.get();
    }
    public void setSupplierName(String name) {
        this.supplierName.set(name);
    }
    public StringProperty supplierNameProperty() { return supplierName; }

    public StringProperty typeProperty() { return type; }
    public String getType() {
        return type.get();
    }
    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty quantityProperty() { return quantity; }
    public int getQuantity() {
        return Integer.parseInt(quantity.get());
    }
    public void setQuantity(int quantity) {
        this.quantity.set(Integer.toString(quantity));
    }

    public StringProperty qualityProperty() { return quality; }
    public int getQuality() {
        return Integer.parseInt(quality.get());
    }
    public void setQuality(int quality) {
        this.quality.set(Integer.toString(quality));
    }

    @Override
    public String toString() {
        return supplierName.get();
    }
}
