package org.falafel;

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                RequestContext context = new RequestContext();
                try {
                    collectResourcesTransaction = capi.createTransaction(
                            TRANSACTION_TIMEOUT, spaceUri, context);
                } catch (MzsCoreException e) {
                    e.printStackTrace();
                    LOGGER.error("Can't create transaction!");
                    return;
                }



                try {
                    containerReference = capi.lookupContainer(
                            FireWorks.MaterialType.Casing.toString(),
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    casing = (Casing) capi.take(containerReference,
                            null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null,  context).get(0);

                    containerReference = capi.lookupContainer(
                            FireWorks.MaterialType.Effect.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    effect = (Effect) capi.take(containerReference,
                           null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context).get(0);

                    containerReference = capi.lookupContainer(
                            FireWorks.MaterialType.Wood.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    wood = (Wood) capi.take(containerReference,
                            null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context).get(0);

                    containerReference = capi.lookupContainer(
                            FireWorks.MaterialType.Propellant.toString(),
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);

                    Propellant lindaTemplate = new Propellant(null, null, null,
                            Propellant.CLOSED);
                    LindaSelector selector = LindaCoordinator.newSelector(
                            lindaTemplate, 1);

                    List<LindaSelector> selectors = new ArrayList<>();
                    selectors.add(selector);
                     propellant = (Propellant) capi.take(containerReference,
                            selectors, RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context).get(0);
                    LOGGER.info("Took propellant: " + propellant.toString());

                    context.setProperty("gotMaterial", true);
                    capi.commitTransaction(
                            collectResourcesTransaction, context);
                    LOGGER.info("Took the following Items: " + casing.toString()
                            + effect.toString()
                            + wood.toString()
                            + propellant.toString());
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
