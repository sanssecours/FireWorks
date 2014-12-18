package org.falafel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents a purchase for one rocket.
 *
 * A purchase is usually part of a larger order which contains multiple
 * purchases.
 *
 */
public class Purchase implements Serializable {

    /** The unique identification of the buyer. */
    private int buyerId;
    /** The identifier for the purchase. This will be the same identifier
     *  for a certain number of purchases, which are all part of a certain
     *  order of a certain buyer. */
    private int purchaseId;
    /** The number of rockets for the whole order which this purchase is
     *  a part of. */
    private int numberRockets;
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
     * @param purchaseId
     *          The identifier for this purchase
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
    Purchase(final int buyerId, final int purchaseId, final int numberRockets,
             final EffectColor firstEffectColor,
             final EffectColor secondEffectColor,
             final EffectColor thirdEffectColor,
             final URI buyerURI) {
        this.buyerId = buyerId;
        this.purchaseId = purchaseId;
        this.numberRockets = numberRockets;
        this.effectColors = new ArrayList<>(
                Arrays.asList(firstEffectColor, secondEffectColor,
                        thirdEffectColor));
        this.buyerURI = buyerURI;
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
                + buyerURI + " Effects: " + effectColors.get(0) + " "
                + effectColors.get(1) + " " + effectColors.get(2)
                + " # Rockets: " + numberRockets + ")";
    }

}
