package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.mozartspaces.core.MzsConstants.RequestTimeout.TRY_ONCE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents the quality tester who decides if a rocket is defect or
 * not. The criteria for a defect rocket are if:
 *      more than one effect charge is faulty
 *      it contains less than 120g of the propellant charge
 */
public final class QualityTester {

    /**
     * Constant for the transaction timeout time.
     */
    private static final int TRANSACTION_TIMEOUT = 3000;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /**
     * Constant for the minimum.
     */
    private static final int MINIMAL_PROPELLANT = 120;
    /**
     * Get the Logger for the current class.
     */
    private static final Logger LOGGER = getLogger(QualityTester.class);
    /** Constant for the minimum propellant to be class A. */
    private static final Integer MINIMAL_PROP_CLASS_A = 130;
    /** The mozart spaces core. */
    private static MzsCore core;
    /** Flag to tell if the program is shutdown. */
    private static boolean shutdown = false;

    /**
     * Create the quality tester singleton.
     */
    private QualityTester() {
    }

    /**
     * Start the quality tester process.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        QualityTester.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        int testerId;
        ArrayList<Rocket> rockets;
        Rocket rocket;
        ArrayList<Effect> effects;

        Capi capi;
        URI spaceUri;
        TransactionReference getRocketsTransaction = null;

        if (arguments.length != 2) {
            System.err.println("Usage: QualityTester <Id> <Space URI>!");
            return;
        }
        try {
            testerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply valid command line arguments!");
            return;
        }

        LOGGER.info("Quality tester " + testerId + " ready to test!");

        ContainerReference container;

        core = DefaultMzsCore.newInstanceWithoutSpace();
        capi = new Capi(core);
        LOGGER.info("Space URI: " + core.getConfig().getSpaceUri());

        ContainerReference benchmarkContainer;
        try {
            benchmarkContainer = capi.lookupContainer("benchmark", spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
        } catch (MzsCoreException e) {
            LOGGER.error("Tester can't find the benchmark container!");
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

        LOGGER.error("Tester " + testerId + ": Starts the Benchmark");

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
                LOGGER.error("Tester " + testerId + ": Benchmark stopped!");
                shutdown = true;
                break;
            }

            try {
                getRocketsTransaction = capi.createTransaction(
                        TRANSACTION_TIMEOUT, spaceUri);
            } catch (MzsCoreException e) {
                LOGGER.error("Can't create transaction!");
                System.exit(1);
            }

            try {
                container = capi.lookupContainer("createdRockets", spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction);
                rockets = capi.take(container,
                        AnyCoordinator.newSelector(1),
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction);

                rocket = rockets.get(0);

                effects = rocket.getEffects();

                int defectCount = 0;
                for (Effect effect : effects) {
                    if (effect.getStatus()) {
                        defectCount++;
                    }
                }
                // more than 1 defect effect the rocket is trashed
                // less than the minimum propellant (120g) the rocket is trashed
                // no defect effect and no less than 130g propellant and the
                // rocket is of quality A
                // every other rocket is quality B

                if (defectCount > 1 || rocket.getPropellantQuantity()
                                                        < MINIMAL_PROPELLANT) {
                    rocket.setQualityClassBad();
                } else if (defectCount == 0 && rocket.getPropellantQuantity()
                                                    >= MINIMAL_PROP_CLASS_A) {
                    rocket.setQualityClassA();
                } else {
                    rocket.setQualityClassB();
                }

                rocket.setTester(testerId);

                container = capi.lookupContainer("testedRockets", spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction);
                capi.write(container, MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction, new Entry(rocket,
                                FifoCoordinator.newCoordinationData()));

                capi.commitTransaction(getRocketsTransaction);
            } catch (MzsTimeoutException toe) {
                LOGGER.debug("Can't finish in transaction time!");
            } catch (CountNotMetException e1) {
                LOGGER.info("Could not get a rocket!");
                try {
                    capi.rollbackTransaction(getRocketsTransaction);
                } catch (MzsCoreException e2) {
                    LOGGER.error("Can't rollback transaction!");
                    System.exit(1);
                }
            } catch (MzsCoreException e) {
                LOGGER.error("Tester has problem with space!");
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
