package org.falafel;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    private Set<EffectColor> effectColors;
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
     * @param effectColors
     *          The colors of the effect for the rocket that should be
     *          produced according to this purchase
     * @param buyerURI
     *          The space URI of the buyer of this rocket
     */
    Purchase(final int buyerId, final int purchaseId, final int numberRockets,
             final Collection<EffectColor> effectColors, final URI buyerURI) {
        this.buyerId = buyerId;
        this.purchaseId = purchaseId;
        this.numberRockets = numberRockets;
        this.effectColors = new HashSet<>();
        this.effectColors.addAll(effectColors);
        this.buyerURI = buyerURI;
    }

}
