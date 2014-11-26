package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static org.mozartspaces.capi3.LindaCoordinator.newCoordinationData;
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
    /** Constant for the lower bound of the working time per element. */
    private static final int LOWERBOUND = 1000;
    /** Constant for the upper bound of the working time per element. */
    private static final int UPPERBOUND = 2000;
    /** Constant for the lower bound of the propellant quantity. */
    private static final int LOWERQUANTITY = 115;
    /** Constant for the upper bound of the propellant quantity. */
    private static final int UPPERQUANTITY = 145;

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);
    /** How many effect charges are needed to build a rocket. */
    private static final int NUMBER_EFFECTS_NEEDED = 3;

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
        ArrayList<Effect> effects;
        HashMap<Propellant, Integer> propellantsWithQuantity = new HashMap<>();
        Wood wood;
        Random randomGenerator = new Random();

        Propellant lindaTemplateClosed = new Propellant(null, null, null,
                Propellant.CLOSED);
        Propellant lindaTemplateOpened = new Propellant(null, null, null,
                Propellant.OPENED);

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
                            MaterialType.Casing.toString(),
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    casing = (Casing) capi.take(containerReference,
                            null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null,  context).get(0);

                    containerReference = capi.lookupContainer(
                            MaterialType.Effect.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    effects = capi.take(containerReference,
                            Arrays.asList(AnyCoordinator.newSelector(
                                          NUMBER_EFFECTS_NEEDED)),
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);

                    containerReference = capi.lookupContainer(
                            MaterialType.Wood.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    wood = (Wood) capi.take(containerReference,
                            null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context).get(0);

                    containerReference = capi.lookupContainer(
                            MaterialType.Propellant.toString(),
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);


                    int propellantQuantity = randomGenerator.nextInt(
                            UPPERQUANTITY - LOWERQUANTITY) + LOWERQUANTITY;

                    ContainerReference container;

                    container = capi.lookupContainer(
                            MaterialType.Propellant.toString(),
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            null);

                    ArrayList<Propellant> result;
                    result = capi.read(container,
                            AnyCoordinator.newSelector(COUNT_ALL),
                            RequestTimeout.TRY_ONCE, null);
                    LOGGER.debug("Propellant container before take: " + result);

                    int takenOpenQuantity = 0;
                    int takenOpenPropellant = 0;
                    int quantity = 0;
                    int missingQuantity = propellantQuantity;
                    propellantsWithQuantity  = new HashMap<>();
                    while (quantity < propellantQuantity) {
                        try {
                            Propellant propellant = (Propellant) capi.take(
                                    containerReference,
                                    Arrays.asList(LindaCoordinator.newSelector(
                                            lindaTemplateOpened)),
                                    RequestTimeout.TRY_ONCE,
                                    collectResourcesTransaction, null,
                                    context).get(0);

                            int currentQuantity = propellant.getQuantity();

                            if (currentQuantity >= missingQuantity) {
                                // Done with rocket
                                quantity = quantity + missingQuantity;
                                propellant.setQuantity(currentQuantity
                                        - missingQuantity);
                                currentQuantity = missingQuantity;
                            } else {
                                // We still need to open a new propellent
                                // after the current one
                                quantity = quantity + currentQuantity;
                                missingQuantity = missingQuantity
                                        - currentQuantity;
                                propellant.setQuantity(0);
                            }

                            takenOpenQuantity = takenOpenQuantity
                                    + currentQuantity;
                            takenOpenPropellant++;
                            propellantsWithQuantity.put(propellant,
                                    currentQuantity);

                        } catch (CountNotMetException e) {
                            // No open propellent available

                            Propellant propellant = (Propellant) capi.take(
                                    containerReference,
                                    Arrays.asList(LindaCoordinator.newSelector(
                                            lindaTemplateClosed)),
                                    RequestTimeout.TRY_ONCE,
                                    collectResourcesTransaction,
                                    null,
                                    context).get(0);

                            int currentQuantity = propellant.getQuantity();
                            quantity = quantity + missingQuantity;

                            propellant.setQuantity(currentQuantity
                                    - missingQuantity);
                            propellantsWithQuantity.put(propellant,
                                    missingQuantity);

                            context.setProperty("takenClosedPropellant", true);
                        }
                    }

                    context.setProperty("takenOpenQuantity",
                            takenOpenQuantity);
                    context.setProperty("takenOpenPropellant",
                            takenOpenPropellant);

                    context.setProperty("gotMaterial", true);
                    capi.commitTransaction(
                            collectResourcesTransaction, context);
                    LOGGER.info("Took the following Items: " + casing.toString()
                            + " " + effects + " "
                            + wood + " "
                            + propellantsWithQuantity.keySet());
                } catch (MzsCoreException e) {
                    LOGGER.info("Could not get all materials in time!");
                    try {
                        capi.rollbackTransaction(collectResourcesTransaction);
                        propellantsWithQuantity.clear();
                    } catch (MzsCoreException e1) {
                        LOGGER.error("Can't rollback transaction!");
                        return;
                    }
                }

                int waitingTime = randomGenerator.nextInt(
                        UPPERBOUND - LOWERBOUND) + LOWERBOUND;

                Thread.sleep(waitingTime);

                ContainerReference container;
                ArrayList<Propellant> result;

                container = capi.lookupContainer(
                        MaterialType.Propellant.toString(),
                        spaceUri,
                        RequestTimeout.TRY_ONCE,
                        null);

                for (Propellant propellant : propellantsWithQuantity.keySet()) {
                    if (propellant.getQuantity() <= 0) {
                        capi.write(container, RequestTimeout.TRY_ONCE, null,
                                new Entry(propellant,
                                        newCoordinationData()));
                    }
                }

                result = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        RequestTimeout.TRY_ONCE, null);
                LOGGER.debug("Propellant after " + result);

            } catch (InterruptedException e) {
                System.out.println("I'm going home.");
                core.shutdown(true);

            } catch (MzsCoreException e) {
                e.printStackTrace();
            }
        }

    }
}
