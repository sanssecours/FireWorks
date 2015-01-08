package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import static org.mozartspaces.core.MzsConstants.RequestTimeout.TRY_ONCE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;

/**
 * This class represents a logistic worker.
 */
public final class Logistic {

    /** Constant for how many rockets are in one package. */
    private static final int PACKAGE_SIZE = 5;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;

    /**
     * Get the Logger for the current class.
     */
    private static final Logger LOGGER = getLogger(Logistic.class);
    /** The mozart spaces core. */
    private static MzsCore core;
    /** The mozart spaces core capi. */
    private static Capi capi;
    /** Flag to tell if the program is shutdown. */
    private static boolean shutdown = false;

    /**
     * Create the quality tester singleton.
     */
    private Logistic() {
    }

    /**
     * Start the quality tester process.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        Logistic.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        int packerId;
        ArrayList<Rocket> rocketsClassA = new ArrayList<>();
        ArrayList<Rocket> rocketsClassB = new ArrayList<>();
        Rocket rocket;
        URI spaceUri;
        Purchase purchase;

        if (arguments.length != 2) {
            System.err.println("Usage: QualityTester <Id> <Space URI>!");
            return;
        }
        try {
            packerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply valid values!");
            return;
        }

        LOGGER.info("Quality tester " + packerId + " ready to test!");

        ContainerReference rocketContainer;
        ContainerReference trashContainer;
        ContainerReference shippingContainer;
        ContainerReference orderedRocketsContainer;
        ContainerReference purchaseContainer;


        core = DefaultMzsCore.newInstanceWithoutSpace();
        capi = new Capi(core);
        LOGGER.info("Space URI: " + core.getConfig().getSpaceUri());

        try {
            rocketContainer = capi.lookupContainer("testedRockets", spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE,
                    null);
            trashContainer = capi.lookupContainer("trashedRockets", spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE,
                    null);
            shippingContainer = capi.lookupContainer("finishedRockets",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE,
                    null);
            orderedRocketsContainer = capi.lookupContainer("orderedRockets",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE,
                    null);
            purchaseContainer = capi.lookupContainer("purchase",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE,
                    null);
        } catch (MzsCoreException e) {
            LOGGER.error("Logistician can't find container!");
            return;
        }

        ContainerReference benchmarkContainer;
        try {
            benchmarkContainer = capi.lookupContainer("benchmark", spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
        } catch (MzsCoreException e) {
            LOGGER.error("Logistician can't find the benchmark container!");
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

        LOGGER.error("Logistician " + packerId + ": Starts the Benchmark");
        while (!shutdown) {
            entry.clear();
            try {
                entry = capi.read(benchmarkContainer,
                        AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE,
                        null);
            } catch (MzsCoreException e) {
                LOGGER.error("Waiting for stop problem");
            }
            if (entry.isEmpty()) {
                LOGGER.error("Logistician " + packerId
                        + ": Benchmark stopped!");
                shutdown = true;
                break;
            }

            try {

                rocket = (Rocket) capi.take(rocketContainer,
                        FifoCoordinator.newSelector(1),
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null).get(0);

                rocket.setPackerId(packerId);
                purchase = rocket.getPurchase();

                switch (rocket.getTestResult()) {
                    case A:
                        if (purchase == null) {
                            rocketsClassA.add(rocket);
                        } else {
                            capi.write(orderedRocketsContainer,
                                    MzsConstants.RequestTimeout.TRY_ONCE,
                                    null, new Entry(rocket));
                        }
                        break;
                    case B:
                        if (purchase != null) {
                            rocket.setPurchase(null);
                        }
                        rocketsClassB.add(rocket);
                        break;
                    case Bad:
                        if (purchase != null) {
                            rocket.setPurchase(null);
                        }
                        capi.write(trashContainer,
                                MzsConstants.RequestTimeout.TRY_ONCE,
                                null, new Entry(rocket));
                        break;
                    default:
                        LOGGER.error("Logistician has found wrong "
                                        + "quality class!");
                        System.exit(1);
                }

                if (rocket.getPurchase() == null && purchase != null) {
                    capi.write(purchaseContainer,
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            null, new Entry(purchase));
                }

                if (rocketsClassA.size() == PACKAGE_SIZE) {
                    capi.write(shippingContainer,
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            null, new Entry(rocketsClassA,
                                    FifoCoordinator.newCoordinationData()));
                    rocketsClassA.clear();
                }
                if (rocketsClassB.size() == PACKAGE_SIZE) {
                    capi.write(shippingContainer,
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            null, new Entry(rocketsClassB,
                                    FifoCoordinator.newCoordinationData()));
                    rocketsClassB.clear();
                }
            } catch (CountNotMetException e1) {
                LOGGER.info("Could not get enough rockets for a package!");

                sendRocketsToContainer(rocketContainer, rocketsClassA);
                rocketsClassA.clear();
                sendRocketsToContainer(rocketContainer, rocketsClassB);
                rocketsClassB.clear();
            } catch (MzsCoreException e) {
                LOGGER.error("Logistician has problem with space!");
                System.exit(1);
            }
        }
        sendRocketsToContainer(rocketContainer, rocketsClassA);
        rocketsClassA.clear();
        sendRocketsToContainer(rocketContainer, rocketsClassB);
        rocketsClassB.clear();
        System.exit(0);
    }

    /**
     * Sends a rocket list to the container.
     *
     * @param container
     *          ContainerReference to which the rockets are written.
     * @param rockets
     *          ArrayList of rockets to write in a container
     */
    private static void sendRocketsToContainer(
                                final ContainerReference container,
                                final ArrayList<Rocket> rockets) {
        for (Rocket returnRocket : rockets) {
            returnRocket.setPackerId(0);
            try {
                capi.write(container,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null, new Entry(returnRocket,
                                FifoCoordinator.newCoordinationData()));
            } catch (MzsCoreException e) {
                LOGGER.error("Logistician can't return rockets to "
                        + "tested container!");
                System.exit(1);
            }
        }
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
