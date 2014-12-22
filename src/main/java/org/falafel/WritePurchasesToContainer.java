package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;

import static org.slf4j.LoggerFactory.getLogger;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;


/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class WritePurchasesToContainer extends Thread {

    /** Constant for the transaction timeout time. */
    private static final int TRANSACTION_TIME_OUT = 3000;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(
            WritePurchasesToContainer.class);
    /** The resource identifier for the space. */
    private final URI spaceUri;
    /** The purchase order which is written to the space. */
    private final Purchase purchase;

    /**
     * Create a new Supplier with a given id.
     *
     * @param space
     *          The resource identifier used to locate the space
     * @param purchase
     *          The purchase order which is written to the space.
     */
    public WritePurchasesToContainer(final URI space,
                                     final Purchase purchase) {
        super();
        spaceUri = space;
        this.purchase = purchase;
    }

    /**
     * Start the writer.
     */
    public final void run() {

        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);

        try {
            ContainerReference container = capi.lookupContainer("purchase",
                spaceUri, MzsConstants.RequestTimeout.TRY_ONCE, null);

            for (int index = 1;
                 index < purchase.getNumberRocketsProperty().intValue();
                 index++) {
                capi.write(container, MzsConstants.RequestTimeout.TRY_ONCE,
                        null, new Entry(purchase));
            }

            ArrayList<Propellant> result;
            result = capi.read(container,
                    AnyCoordinator.newSelector(COUNT_ALL),
                    RequestTimeout.TRY_ONCE, null);
        } catch (MzsCoreException e) {
                LOGGER.error("Can't write purchases to space!");
        }

        core.shutdown(true);
    }
}
