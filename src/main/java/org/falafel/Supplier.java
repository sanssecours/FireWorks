package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;

import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class Supplier extends Thread {

    /** Save the (unique) identifier for this supplier. */
    private final int id;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);
    /** The resource identifier for the space. */
    private final URI spaceUri;
    /** The order which the supplier shipped. */
    private final SupplyOrder order;

    /**
     * Create a new Supplier with a given id.
     *
     * @param identifier
     *          The (unique) identifier for this supplier
     * @param space
     *          The resource identifier used to locate the space
     * @param order
     *          The order the supplier should provide
     */
    public Supplier(final int identifier, final URI space,
                    final SupplyOrder order) {
        super();
        id = identifier;
        spaceUri = space;
        this.order = order;
    }

    /**
     * Start the supplier.
     */
    public final void run() {
        ContainerReference woodContainer;
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);
        ArrayList<Wood> result;

        Material newEntry;

        System.out.println("Supplier " + id + " active!");

        if (order.getType().equals(
                FireWorks.MaterialType.Casing.toString())) {
            newEntry = new Wood(id, order.getSupplierName(), id);
        } else if (order.getType().equals(
                FireWorks.MaterialType.Effect.toString())) {
            newEntry = new Wood(id, order.getSupplierName(), id);
        } else if (order.getType().equals(
                FireWorks.MaterialType.Propellant.toString())) {
            newEntry = new Wood(id, order.getSupplierName(), id);
        } else {
            newEntry = new Wood(id, order.getSupplierName(), id);
        }

        try {
            woodContainer = capi.lookupContainer("Wood", spaceUri,
                    RequestTimeout.TRY_ONCE, null);
            capi.write(woodContainer, new Entry(newEntry));
            LOGGER.debug("Supplier " + id + " Wrote entry to container.");
            result = capi.read(woodContainer,
                    AnyCoordinator.newSelector(COUNT_ALL),
                    RequestTimeout.TRY_ONCE, null);
            LOGGER.debug("Supplier " + id + " Read: " + result.toString());
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }

        core.shutdown(true);
    }
}
