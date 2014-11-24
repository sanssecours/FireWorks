package org.falafel;

import jdk.nashorn.internal.ir.CatchNode;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import static org.falafel.FireWorks.MaterialType.Casing;
import static org.falafel.FireWorks.MaterialType.Effect;
import static org.falafel.FireWorks.MaterialType.Wood;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.mozartspaces.core.MzsConstants.RequestTimeout;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a worker. A worker collects material from the space
 * and uses them to create a rocket.
 */
public final class Worker {

    /** Constant for the transaction timeout time. */
    private static final int TRANSACTION_TIMEOUT = 5000;
    /** Constant for the lower bound of the loading time per element. */
    private static final int LOWERBOUND = 1000;
    /** Constant for the upper bound of the loading time per element. */
    private static final int UPPERBOUND = 2000;

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
        Effect effect;
        Propellant propellant;
        Wood wood;
        Random randomGenerator = new Random();

        Capi capi;
        MzsCore core;
        URI spaceUri;
        TransactionReference collectResourcesTransaction;
        if (arguments.length != 2) {
            System.err.println("Usage: worker <Id> <Space URI>!");
            return;
        }

        try {
            workerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply a valid values!");
            return;
        }

        LOGGER.info("Worker " + workerId + " ready to work!");

        ContainerReference containerReference;

        core = DefaultMzsCore.newInstanceWithoutSpace();
        capi = new Capi(core);
        LOGGER.info("Space URI: " + core.getConfig().getSpaceUri());

        while (true) {
            try {
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
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction);
                    casing = (Casing) capi.take(containerReference,
                            AnyCoordinator.newSelector(1),
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction).get(0);

                    containerReference = capi.lookupContainer(
                            Effect.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction);
                    effect = (Effect) capi.take(containerReference,
                            AnyCoordinator.newSelector(1),
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction).get(0);

                    containerReference = capi.lookupContainer(
                            Wood.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction);
                    wood = (Wood) capi.take(containerReference,
                            AnyCoordinator.newSelector(1),
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction).get(0);

                    capi.commitTransaction(collectResourcesTransaction);
                    LOGGER.info("Took the following Items: " + casing.toString()
                            + effect.toString()
                            + wood.toString());
                } catch (MzsCoreException e) {
                    LOGGER.info("Could not get all materials in time!");
                    try {
                        capi.rollbackTransaction(collectResourcesTransaction);
                    } catch (MzsCoreException e1) {
                        System.err.println("Can't rollback transaction!");
                    }
                }


                int waitingTime = randomGenerator.nextInt(
                        UPPERBOUND - LOWERBOUND) + LOWERBOUND;
//                System.out.println("Waiting Time: " + waitingTime);
                Thread.sleep(waitingTime);

            } catch (InterruptedException e) {
                System.out.println("I'm going home.");
                core.shutdown(true);
            }
        }

    }
}
