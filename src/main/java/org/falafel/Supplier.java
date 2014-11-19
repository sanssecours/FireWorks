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

    /**
     * Create a new Supplier with a given id.
     *
     * @param identifier
     *          The (unique) identifier for this supplier
     * @param space
     *          The resource identifier used to locate the space
     */
    public Supplier(final int identifier, final URI space) {
        super();
        id = identifier;
        spaceUri = space;
    }

    /**
     * Start the supplier.
     */
    public final void run() {
        ContainerReference woodContainer;
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);
        ArrayList<Wood> result;


        System.out.println("Supplier " + id + " active!");

        try {
            woodContainer = capi.lookupContainer("Wood", spaceUri,
                    RequestTimeout.TRY_ONCE, null);
            capi.write(woodContainer, new Entry(new Wood(id)));
            LOGGER.debug("Wrote entry to container.");
            result = capi.read(woodContainer,
                    AnyCoordinator.newSelector(COUNT_ALL),
                    RequestTimeout.TRY_ONCE, null);
            LOGGER.debug("Read: " + result.toString());
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }

        core.shutdown(true);
    }
}
