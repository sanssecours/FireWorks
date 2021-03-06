package org.falafel;

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * This class represents a writer which tries to sends a finished purchase order
 * to the buyer.
 */
public class WriteFinishedPurchasesToBuyer extends Thread {

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(
            WriteFinishedPurchasesToBuyer.class);
    /** The resource identifier for the buyer space. */
    private final URI buyerSpaceUri;
    /** The resource identifier for the FireWorks space. */
    private final URI fireWorksSpaceUri;
    /** The purchase order which is written to the space. */
    private final Purchase purchase;

    /**
     * Create a new Writer to send the finished purchase to the buyer.
     *
     * @param fireWorksSpace
     *          The resource identifier used to locate the space
     * @param purchase
     *          The purchase order which is written to the buyer.
     */
    public WriteFinishedPurchasesToBuyer(final URI fireWorksSpace,
                                         final Purchase purchase) {
        super();
        buyerSpaceUri = URI.create(purchase.getBuyerURI().getValue());
        fireWorksSpaceUri = fireWorksSpace;
        this.purchase = purchase;
    }

    /**
     * Start the writer.
     */
    public final void run() {

        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);
        ArrayList<Rocket> rockets;

        Purchase purchaseTemplate = new Purchase(
                purchase.getBuyerId().intValue(),
                purchase.getPurchaseId().intValue(),
                null, null, null, null, null);
        Rocket rocketTemplate = new Rocket(null, null, null, null, null,
                null, null, purchaseTemplate);

        ContainerReference buyerContainer;
        ContainerReference fireWorksContainer;

        System.out.println("Shipping to buyer: "
                + purchase.getBuyerId().intValue() + " purchase: "
                + purchase.getPurchaseId().intValue() + " starts!");
        try {
            buyerContainer = capi.lookupContainer("rockets",
                    buyerSpaceUri, MzsConstants.RequestTimeout.TRY_ONCE, null);
        } catch (MzsCoreException e) {
            LOGGER.info("Can not find buyer space!");
            return;
        }
        try {
            fireWorksContainer = capi.lookupContainer("orderedRockets",
                    fireWorksSpaceUri, MzsConstants.RequestTimeout.TRY_ONCE,
                    null);
        } catch (MzsCoreException e) {
            LOGGER.error("Can not find orderedRocket container in the "
                    + "FireWorks space!");
            return;
        }

        try {
            rockets = capi.take(
                    fireWorksContainer,
                    asList(LindaCoordinator.newSelector(rocketTemplate,
                            COUNT_ALL)),
                    MzsConstants.RequestTimeout.TRY_ONCE,
                    null, null, null);
            System.out.println(rockets);
        } catch (MzsCoreException e) {
            LOGGER.error("Can not get the rockets of the purchase!");
            return;
        }

        try {
            for (Rocket rocket : rockets) {
                capi.write(buyerContainer, MzsConstants.RequestTimeout.TRY_ONCE,
                        null, new Entry(rocket));
            }
        } catch (MzsCoreException e) {
                LOGGER.error("Can't write to buyer space!");
        }
        purchase.setStatusToShipped();
        FireWorks.updatePurchaseTable(purchase);
        core.shutdown(true);
    }
}
