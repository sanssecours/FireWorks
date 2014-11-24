package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.net.URI;

import static org.falafel.FireWorks.MaterialType.Casing;
import static org.mozartspaces.core.MzsConstants.RequestTimeout;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a worker. A worker collects material from the space
 * and uses them to create a rocket.
 */
public final class Worker {

    /** Constant for the transaction timeout time. */
    private static final int TRANSACTION_TIMEOUT = 5000;

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);

    /** Create the worker singleton. */
    private Worker() { }

    /**
     * Start the worker process.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {

        int workerId;

        Casing casing;

        Capi capi;
        MzsCore core;
        URI spaceUri;
        TransactionReference collectResourcesTransaction;

        if (arguments.length < 1) {
            System.err.println("Please supply an ID!");
            return;
        }

        try {
            workerId = Integer.parseInt(arguments[0]);
        } catch (NumberFormatException e) {
            System.err.println("Please supply a valid ID!");
            return;
        }

        LOGGER.info("Worker " + workerId + " ready to work!");

        ContainerReference containerReference;

        core = DefaultMzsCore.newInstanceWithoutSpace();
        capi = new Capi(core);
        spaceUri = URI.create("xvsm://localhost:9876");
        LOGGER.info("Space URI: " + spaceUri);

        try {
            collectResourcesTransaction = capi.createTransaction(
                    TRANSACTION_TIMEOUT, spaceUri);
        } catch (MzsCoreException e) {
            e.printStackTrace();
            LOGGER.error("Can't create transaction!");
            return;
        }

        try {
            containerReference = capi.lookupContainer(
                    Casing.toString(), spaceUri,
                    RequestTimeout.TRY_ONCE, collectResourcesTransaction);
            casing = (Casing) capi.take(containerReference,
                    AnyCoordinator.newSelector(1), RequestTimeout.TRY_ONCE,
                    collectResourcesTransaction).get(0);
            LOGGER.info("Took the following casing: " + casing.toString());
        } catch (CountNotMetException e) {
            LOGGER.info("One of the materials is not available");
        } catch (MzsCoreException e) {
            LOGGER.info("Could not get all materials in time!");
        }
        core.shutdown(true);

    }
}
