package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.mozartspaces.capi3.LindaCoordinator.newCoordinationData;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.mozartspaces.core.MzsConstants.RequestTimeout;
import static org.mozartspaces.core.MzsConstants.RequestTimeout.TRY_ONCE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a worker. A worker collects material from the space
 * and uses them to create a rocket.
 */
public final class Worker {

    /** Constant for the transaction timeout time. */
    private static final int TRANSACTION_TIMEOUT = 5000;
    /** Specifies how long a worker waits until he tries to get new material
     *  after he failed the last time. */
    /** Constant for the lower bound of the propellant quantity. */
    private static final int LOWERQUANTITY = 115;
    /** Constant for the upper bound of the propellant quantity. */
    private static final int UPPERQUANTITY = 145;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Worker.class);
    /** How many effect charges are needed to build a rocket. */
    private static final int NUMBER_EFFECTS_NEEDED = 3;
    /** The mozart spaces core. */
    private static MzsCore core;
    /** Flag to tell if the program is shutdown. */
    private static boolean shutdown = false;

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
        Worker.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        int workerId;
        boolean gotPurchase;
        Random randomGenerator = new Random();
        int propellantQuantity = 0;

        Propellant lindaTemplateClosed = new Propellant(null, null, null,
                Propellant.CLOSED);
        Propellant lindaTemplateOpened = new Propellant(null, null, null,
                Propellant.OPENED);

        Capi capi;
        URI spaceUri;
        TransactionReference collectResourcesTransaction = null;
        Casing casing = null;
        ArrayList<Effect> effects = new ArrayList<>();
        HashMap<Propellant, Integer> propellantsWithQuantity =
                new HashMap<>();
        Wood wood = null;

        if (arguments.length != 2) {
            System.err.println("Usage: worker <Id> <Space URI>!");
            return;
        }

        try {
            workerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply valid command line arguments!");
            return;
        }

        LOGGER.info("Worker " + workerId + " ready to work!");

        ContainerReference containerReference;
        ContainerReference benchmarkContainer;

        core = DefaultMzsCore.newInstanceWithoutSpace();
        capi = new Capi(core);
        LOGGER.info("Space URI: " + core.getConfig().getSpaceUri());

        try {
            benchmarkContainer = capi.lookupContainer("benchmark", spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
        } catch (MzsCoreException e) {
            LOGGER.error("Worker can't find the benchmark container!");
            return;
        }

        ArrayList<Serializable> entry = new ArrayList<>();
        while (entry.isEmpty()) {
            try {
                entry = capi.read(benchmarkContainer,
                        AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            } catch (MzsCoreException e) {
                LOGGER.error("Waiting for start");
            }
        }

        LOGGER.error("Start the Benchmark");
        while (!shutdown) {
            try {
                entry.clear();
                try {
                    entry = capi.read(benchmarkContainer,
                            AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE,
                            null);
                } catch (MzsCoreException e) {
                    LOGGER.error("Waiting for stop problem");
                }
                if (entry.isEmpty()) {
                    LOGGER.error("Worker: Benchmark stopped!");
                    shutdown = true;
                    break;
                }

                RequestContext context = new RequestContext();
                Purchase purchase = null;
                gotPurchase = false;

                try {
                    collectResourcesTransaction = capi.createTransaction(
                            TRANSACTION_TIMEOUT, spaceUri, context);
                } catch (MzsCoreException e) {
                    LOGGER.error("Can't create transaction!");
                    System.exit(1);
                }

                try {
                    containerReference = capi.lookupContainer(
                            "purchase",
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    purchase = (Purchase) capi.take(containerReference,
                            null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context).get(0);
                    gotPurchase = true;
                } catch (MzsTimeoutException toe) {
                    LOGGER.debug("Can't finish in transaction time!");
                } catch (CountNotMetException e1) {
                    LOGGER.info("No purchase order, create random rocket!");
                    gotPurchase = false;
                } catch (MzsCoreException e) {
                    LOGGER.error("Worker has problem with space!");
                    System.exit(1);
                }


                try {
                    effects.clear();
                    containerReference = capi.lookupContainer(
                            MaterialType.Effect.toString(), spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);

                    if (gotPurchase) {
                        Collection<EffectColor> colors =
                                purchase.getEffectColors();
                        Effect effect;
                        for (EffectColor color : colors) {
                            Effect lindaEffectColorTemplate =
                                    new Effect(null, null, null, null,
                                    color);
                            try {
                                effect = (Effect) capi.take(
                                        containerReference,
                                        asList(
                                                LindaCoordinator.newSelector(
                                                        lindaEffectColorTemplate
                                                )),
                                        RequestTimeout.TRY_ONCE,
                                        collectResourcesTransaction,
                                        null, context).get(0);
                                effects.add(effect);
                            } catch (CountNotMetException e) {
                                LOGGER.info("Not enough effect charges of "
                                        + "color: " + color.toString());
                                gotPurchase = false;
                                break;
                            }
                        }
                    }
                    // if not enough effect charges of the colors needed for the
                    // purchase are available, make a new random rocket by
                    // filling the missing effect charges with random colors
                    if (!gotPurchase) {
                        int missingEffects = NUMBER_EFFECTS_NEEDED
                                - effects.size();
                        ArrayList<Effect> tempEffects;
                        tempEffects = capi.take(containerReference,
                                asList(AnyCoordinator.newSelector(
                                        missingEffects)),
                                RequestTimeout.TRY_ONCE,
                                collectResourcesTransaction, null, context);
                        effects.addAll(tempEffects);
                    }

                    context.setProperty("color1", effects.get(0).getColor());
                    context.setProperty("color2", effects.get(1).getColor());
                    context.setProperty("color3", effects.get(2).getColor());

                    containerReference = capi.lookupContainer(
                            MaterialType.Casing.toString(),
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context);
                    casing = (Casing) capi.take(containerReference,
                            null,
                            RequestTimeout.TRY_ONCE,
                            collectResourcesTransaction, null, context).get(0);

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

                    propellantQuantity = randomGenerator.nextInt(
                            UPPERQUANTITY - LOWERQUANTITY) + LOWERQUANTITY;

                    int takenOpenQuantity = 0;
                    int takenOpenPropellant = 0;
                    int quantity = 0;
                    int missingQuantity = propellantQuantity;
                    propellantsWithQuantity  = new HashMap<>();
                    while (quantity < propellantQuantity) {
                        try {
                            Propellant propellant = (Propellant) capi.take(
                                    containerReference,
                                    asList(LindaCoordinator.newSelector(
                                            lindaTemplateOpened)),
                                    RequestTimeout.TRY_ONCE,
                                    collectResourcesTransaction, null,
                                    context).get(0);

                            int currentQuantity = propellant.getQuantity();

                            takenOpenQuantity = takenOpenQuantity
                                    + currentQuantity;
                            takenOpenPropellant++;

                            if (currentQuantity >= missingQuantity) {
                                // Done with rocket
                                quantity = quantity + missingQuantity;
                                propellant.setQuantity(currentQuantity
                                        - missingQuantity);
                                currentQuantity = missingQuantity;
                            } else {
                                // We still need to open a new propellant
                                // after the current one
                                quantity = quantity + currentQuantity;
                                missingQuantity = missingQuantity
                                        - currentQuantity;
                                propellant.setQuantity(0);
                            }
                            propellantsWithQuantity.put(propellant,
                                    currentQuantity);

                        } catch (CountNotMetException e) {
                            // No open propellant available
                            Propellant propellant = (Propellant) capi.take(
                                    containerReference,
                                    asList(LindaCoordinator.newSelector(
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

                    context.setProperty("takenOpenQuantity", takenOpenQuantity);
                    context.setProperty("takenOpenPropellant",
                            takenOpenPropellant);

                    context.setProperty("gotMaterial", true);
                    capi.commitTransaction(
                            collectResourcesTransaction, context);
                    LOGGER.info("Took the following Items: " + casing
                            + " " + effects + " "
                            + wood + " "
                            + propellantsWithQuantity.keySet());
                } catch (MzsTimeoutException toe) {
                    LOGGER.debug("Can't get materials in transaction time!");
                    continue;
                } catch (MzsCoreException e) {
                    LOGGER.info("Worker " + workerId
                            + ": Could not get all materials!");
                    try {
                        capi.rollbackTransaction(collectResourcesTransaction);
                        propellantsWithQuantity.clear();
                        continue;
                    } catch (MzsCoreException e1) {
                        LOGGER.error("Can't rollback transaction!");
                        System.exit(1);
                    }
                }

                // if we got a purchase but created a Random rocket we write the
                // purchase back in the container
                if (!gotPurchase && purchase != null) {
                    containerReference = capi.lookupContainer(
                            "purchase",
                            spaceUri,
                            RequestTimeout.TRY_ONCE,
                            null);
                    capi.write(containerReference, RequestTimeout.TRY_ONCE,
                            null, new Entry(purchase));
                }

                Rocket producedRocket;
                // Worker produces rocket
                if (gotPurchase) {
                    producedRocket = new Rocket(1, wood, casing, effects,
                            propellantsWithQuantity, propellantQuantity,
                            workerId, purchase);
                } else {
                    producedRocket = new Rocket(1, wood, casing, effects,
                            propellantsWithQuantity, propellantQuantity,
                            workerId);
                }

                wood = null;

                // Worker writes the new rocket in the container
                ContainerReference container;
                container = capi.lookupContainer(
                        "createdRockets",
                        spaceUri,
                        RequestTimeout.TRY_ONCE,
                        null);
                capi.write(container, RequestTimeout.TRY_ONCE, null,
                        new Entry(producedRocket));

                // write the used propellant package back if it still contains
                // propellant
                container = capi.lookupContainer(
                        MaterialType.Propellant.toString(),
                        spaceUri,
                        RequestTimeout.TRY_ONCE,
                        null);

                for (Propellant propellant : propellantsWithQuantity.keySet()) {
                    if (propellant.getQuantity() > 0) {
                        capi.write(container, RequestTimeout.TRY_ONCE, null,
                                new Entry(propellant,
                                        newCoordinationData()));
                    }
                }
            } catch (MzsCoreException e) {
                //e.printStackTrace();
                LOGGER.error("Worker has s space problem");
                System.exit(1);
            }
        }
        System.exit(0);
    }

    /**
     * adds a shutdown hook (called before shutdown).
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("I'm packing my stuff together.");
                shutdown = true;
                try {
                    Thread.sleep(WAIT_TIME_TO_SHUTDOWN);
                } catch (InterruptedException e) {
                    LOGGER.error("I was interrupted while trying to sleep. "
                            + "How rude!");
                }
                core.shutdown(true);
                System.out.println("I'm going home.");
            }
        });
    }
}
