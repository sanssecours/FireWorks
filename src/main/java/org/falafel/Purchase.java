package org.falafel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static org.falafel.Purchase.PurchaseStatus.Processing;
import static org.falafel.Purchase.PurchaseStatus.Finished;
import static org.falafel.Purchase.PurchaseStatus.Shipped;

import static org.falafel.EffectColor.Blue;
import static org.falafel.EffectColor.Green;
import static org.falafel.EffectColor.Red;

/**
 * This class represents a purchase for one rocket.
 *
 * A purchase is usually part of a larger order which contains multiple
 * purchases.
 *
 */
public class Purchase implements Serializable {
    /** The different states a purchase can be in. */
    public enum PurchaseStatus { Processing, Finished, Shipped }

    /** The purchase id for the next purchase. */
    private static int nextPurchaseId = 0;

    /** The unique identification of the buyer. */
    private int buyerId;
    /** The identifier for the purchase. This will be the same identifier
     *  for a certain number of purchases, which are all part of a certain
     *  order of a certain buyer. */
    private int purchaseId;
    /** The number of rockets for the whole order which this purchase is
     *  a part of. */
    private int numberRockets;
    /** The number of rockets already ready for sale. */
    private int numberFinishedRockets;
    /** The status of the purchase. */
    private PurchaseStatus status;

    /** The colors of the three effects that should be part of the produced
     *  rocket for this purchase. */
    private ArrayList<EffectColor> effectColors;
    /** The URI for the space of the buyer of this rocket. */
    private URI buyerURI;

    /**
     * Create a new purchase with the given arguments.
     *
     * @param buyerId
     *          The identifier of the buyer
     * @param numberRockets
     *          The number of rockets which this purchase is a part of
     * @param firstEffectColor
     *          The color of the first effect for the rocket that should be
     *          produced according to this purchase
     * @param secondEffectColor
     *          The color of the second effect for the rocket
     * @param thirdEffectColor
     *          The color of the third effect for the rocket
     * @param buyerURI
     *          The space URI of the buyer of this rocket
     */
    Purchase(final int buyerId, final int numberRockets,
             final EffectColor firstEffectColor,
             final EffectColor secondEffectColor,
             final EffectColor thirdEffectColor,
             final URI buyerURI) {
        this.buyerId = buyerId;
        this.purchaseId = nextPurchaseId++;
        this.numberRockets = numberRockets;
        this.numberFinishedRockets = 0;
        this.effectColors = new ArrayList<>(
                Arrays.asList(firstEffectColor, secondEffectColor,
                        thirdEffectColor));
        this.buyerURI = buyerURI;
        status = Processing;
    }

    /**
     * Create a new purchase with default arguments.
     *
     * @param buyerId
     *          The identifier of the buyer
     */
    Purchase(final int buyerId) {
        this(buyerId, 1, Red, Green, Blue, URI.create("xvsm://localhost:9876"));
    }

    /**
     * Get the id of the purchase.
     *
     * @return The identifier for this purchase
     */
    public final IntegerProperty getPurchaseId() {
        return new SimpleIntegerProperty(purchaseId);
    }

    /**
     * Get the id of the buyer for the purchase.
     *
     * @return The identifier of the buyer
     */
    public final IntegerProperty getBuyerId() {
        return new SimpleIntegerProperty(buyerId);
    }

    /**
     * Get the space URI of the buyer for the purchase.
     *
     * @return
     *          The URI of the buyer of this purchase
     */
    public final StringProperty getBuyerURI() {
        return new SimpleStringProperty(buyerURI.toString());
    }

    /**
     * Return the number of rockets for the whole purchase.
     *
     * @return The number of rockets for the purchase.
     */
    public final IntegerProperty getNumberRocketsProperty() {
        return new SimpleIntegerProperty(numberRockets);
    }

    /**
     * Return the number of rockets that are ready for sale for this purchase.
     *
     * @return The number of finished rockets
     */
    public final IntegerProperty getNumberFinishedRocketsProperty() {
        return new SimpleIntegerProperty(numberFinishedRockets);
    }

    /**
     * Return all effect color in a short form using only the initial letter
     * of the colors.
     *
     * @return The colors of the effects stored in this purchase
     */
    public final StringProperty getEffectColors() {
        String colors = "";
        for (EffectColor color : effectColors) {
            colors += color.toString().substring(0, 1);
        }
        return new SimpleStringProperty(colors);
    }

    /**
     * Return the color of the first effect for this purchase.
     *
     * @return The color of the first effect.
     */
    public final StringProperty getFirstColorProperty() {
        return new SimpleStringProperty(effectColors.get(0).toString());
    }

    /**
     * Return the color of the second effect for this purchase.
     *
     * @return The color of the second effect.
     */
    public final StringProperty getSecondColorProperty() {
        return new SimpleStringProperty(effectColors.get(1).toString());
    }

    /**
     * Return the color of the third effect for this purchase.
     *
     * @return The color of the third effect.
     *
     */
    public final StringProperty getThirdColorProperty() {
        return new SimpleStringProperty(effectColors.get(2).toString());
    }

    /**
     * Represent the purchase as a string.
     *
     * @return The string representation of this purchase.
     */
    public final String toString() {
        return "(PURCHASE " + purchaseId + " — Buyer: " + buyerId + " "
                + buyerURI + " Effects: " + getEffectColors()
                + " # Rockets: " + numberRockets + ")";
    }

    /**
     * Change the number of rockets for this purchase.
     *
     * @param quantity
     *          The new effect color for the first effect.
     */
    public final void setNumberRockets(final String quantity) {
        this.numberRockets = Integer.parseInt(quantity);
    }

    /**
     * Increase the number of finished rockets for this purchase.
     *
     * @param numberAdditionalFinishedRockets
     *          The quantity of newly finished rockets
     */
    @SuppressWarnings("unused")
    public final void addFinishedRockets(
            final int numberAdditionalFinishedRockets) {
        this.numberFinishedRockets += numberAdditionalFinishedRockets;
    }

    /**
     * Change the color of the first effect.
     *
     * @param effectColor
     *          The new effect color for the first effect.
     */
    public final void setFirstEffectColor(final String effectColor) {
        this.effectColors.set(0, EffectColor.valueOf(effectColor));
    }

    /**
     * Change the color of the second effect.
     *
     * @param effectColor
     *
     *          The new effect color for the second effect.
     */
    public final void setSecondEffectColor(final String effectColor) {
        this.effectColors.set(1, EffectColor.valueOf(effectColor));
    }

    /**
     * Change the color of the third effect.
     *
     * @param effectColor
     *
     *          The new effect color for the third effect.
     */
    public final void setThirdEffectColor(final String effectColor) {
        this.effectColors.set(1, EffectColor.valueOf(effectColor));
    }

    /**
     * Change the status of the purchase to Finished.
     */
    @SuppressWarnings("unused")
    public final void setStatusToFinished() {
        status = Finished;
    }

    /**
     * Change the status of the purchase to Shipped.
     */
    @SuppressWarnings("unused")
    public final void setStatusToShipped() {
        status = Shipped;
    }

    /**
     * Return the status of this purchase.
     *
     * @return The status of the purchase.
     *
     */
    public final StringProperty getStatusProperty() {
        return new SimpleStringProperty(status.toString());
    }
}
