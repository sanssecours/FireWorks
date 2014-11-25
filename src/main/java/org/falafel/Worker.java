package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
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
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    private static final int LOWERQUANTITY = 115;
    private static final int UPPERQUANTITY = 145;

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
        ArrayList<Propellant> propellants = new ArrayList<>();
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


                    int propellantQuantity = randomGenerator.nextInt(
                            UPPERQUANTITY - LOWERQUANTITY) + LOWERQUANTITY;

                    ContainerReference container = null;

                    container = capi.lookupContainer(FireWorks.MaterialType.Propellant.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE, null);

                    ArrayList<Propellant> result;
                    result = capi.read(container,
                            AnyCoordinator.newSelector(COUNT_ALL),
                            RequestTimeout.TRY_ONCE, null);
                    System.err.println("Propellant before " + result);

                    int quantity = 0;
                    int missingQuantity = propellantQuantity;
                    propellants = new ArrayList<>();
                    while (quantity < propellantQuantity) {
                        try {
                            System.err.println("Try Open");
                            Propellant propellant = (Propellant) capi.take(containerReference,
                                    Arrays.asList(LindaCoordinator.newSelector(lindaTemplateOpened)), RequestTimeout.TRY_ONCE,
                                    collectResourcesTransaction, null,
                                    context).get(0);

                            System.err.println("Got Open");
                            int currentQuantity = propellant.getQuantity();
                            if (currentQuantity >= missingQuantity) {
                                quantity = quantity + missingQuantity;
                                propellant.setQuantity(currentQuantity
                                        - missingQuantity);
                            } else {
                                quantity = quantity + currentQuantity;
                                missingQuantity = missingQuantity - currentQuantity;
                                propellant.setQuantity(0);
                            }

                            propellants.add(propellant);

                        } catch (MzsCoreException e) {
                            System.err.println("Try Closes " + e);

                            Propellant propellant = (Propellant) capi.take(containerReference,
                                    Arrays.asList(LindaCoordinator.newSelector(lindaTemplateClosed)), RequestTimeout.TRY_ONCE,
                                    collectResourcesTransaction, null,
                                    context).get(0);

                            System.err.println("Test: "
                                    + propellant);
                            int currentQuantity = propellant.getQuantity();

                            quantity = quantity + missingQuantity;

                            propellant.setQuantity(currentQuantity
                                    - missingQuantity);
                            propellants.add(propellant);

                            System.err.println("Closed: Remaining quantity: "
                                    + propellant.getQuantity());
                        }
                        System.err.println("Collected Quantity: " + quantity + " -- Needed Quantity: " + propellantQuantity);
                    }



                    context.setProperty("gotMaterial", true);
                    capi.commitTransaction(
                            collectResourcesTransaction, context);
                    LOGGER.info("Took the following Items: " + casing.toString()
                            + effect.toString()
                            + wood.toString()
                            + propellants.toString());
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


                ContainerReference container;
                ArrayList<Propellant> result;

                container = capi.lookupContainer(FireWorks.MaterialType.Propellant.toString(), spaceUri,
                        RequestTimeout.TRY_ONCE, null);
                System.err.println("Propellant befor writing back: " + propellants);
                for(int i = 0; i < propellants.size(); i++)
                {
                    if (propellants.get(i).getQuantity() != 0) {
                        capi.write(container, RequestTimeout.TRY_ONCE, null,
                                new Entry(propellants.get(i), LindaCoordinator.newCoordinationData()));
                    }
                }

                result = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        RequestTimeout.TRY_ONCE, null);
                System.err.println("Propellant after " + result);



            } catch (InterruptedException e) {
                System.out.println("I'm going home.");
                core.shutdown(true);
            } catch (MzsCoreException e) {
                e.printStackTrace();
            }
        }

    }
}
