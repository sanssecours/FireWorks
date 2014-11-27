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
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;

import static org.mozartspaces.capi3.Selector.COUNT_ALL;
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
    /** Specifies how long a tester waits until he tries to get a new rocket
     *  after he was unable to get one the last time. */
    private static final int WAIT_TIME_TESTER_MS = 2000;
    /**
     * Constant for the minimum .
     */
    private static final int MINIMAL_PROPELLANT = 120;
    /**
     * Get the Logger for the current class.
     */
    private static final Logger LOGGER = getLogger(FireWorks.class);

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
        int testerId;
        ArrayList<Rocket> rockets;
        Rocket rocket;
        ArrayList<Effect> effects;

        Capi capi;
        MzsCore core;
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

        while (true) {
            try {
                getRocketsTransaction = capi.createTransaction(
                        TRANSACTION_TIMEOUT, spaceUri);
            } catch (MzsCoreException e) {
                //e.printStackTrace();
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
                if (defectCount > 1 || rocket.getPropellantQuantity()
                                                        < MINIMAL_PROPELLANT) {
                    rocket.setTestResult(true);
                } else {
                    rocket.setTestResult(false);
                }

                rocket.setTester(testerId);

                container = capi.lookupContainer("testedRockets", spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction);
                capi.write(container, MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction, new Entry(rocket,
                                FifoCoordinator.newCoordinationData()));

                capi.commitTransaction(getRocketsTransaction);
            } catch (CountNotMetException e1) {
                LOGGER.info("Could not get a rocket in time!");
                try {
                    Thread.sleep(WAIT_TIME_TESTER_MS);
                } catch (InterruptedException e) {
                    LOGGER.error("I was interrupted while trying to sleep. "
                            + "How rude!");
                }
                try {
                    capi.rollbackTransaction(getRocketsTransaction);
                } catch (MzsCoreException e2) {
                    LOGGER.error("Can't rollback transaction!");
                    System.exit(1);
                }
            } catch (MzsCoreException e) {
                //e.printStackTrace();
                LOGGER.error("Tester has problem with space!");
                System.exit(1);
            }
            try {
                container = capi.lookupContainer(
                        "createdRockets",
                        spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null);
                ArrayList<Rocket> readRocket;
                readRocket = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                LOGGER.debug("Rockets still created container " + readRocket);

                container = capi.lookupContainer(
                        "testedRockets",
                        spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null);
                readRocket = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                LOGGER.debug("Rockets in tested container " + readRocket);
            } catch (MzsCoreException e) {
                //e.printStackTrace();
                LOGGER.error("Tester has problem with space!");
                System.exit(1);
            }
        }
    }
}
