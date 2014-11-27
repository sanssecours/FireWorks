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
 * This class represents a logistic worker.
 */
public final class Logistic {

    /**
     * Constant for the transaction timeout time.
     */
    private static final long TRANSACTION_TIMEOUT =
                                        MzsConstants.RequestTimeout.INFINITE;
    /**
     * Get the Logger for the current class.
     */
    private static final Logger LOGGER = getLogger(FireWorks.class);

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
        int packerId;
        ArrayList<Rocket> rockets;
        ArrayList<Rocket> functioningRockets;
        ArrayList<Rocket> trashedRockets;
        Rocket rocket;
        Capi capi;
        MzsCore core;
        URI spaceUri;
        TransactionReference getRocketsTransaction = null;

        if (arguments.length != 2) {
            System.err.println("Usage: QualityTester <Id> <Space URI>!");
            return;
        }
        try {
            packerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply a valid values!");
            return;
        }

        LOGGER.info("Quality tester " + packerId + " ready to test!");

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
                LOGGER.error("Logistician can't create transaction!");
                System.exit(1);
            }

            try {
                functioningRockets = new ArrayList<>();
                trashedRockets = new ArrayList<>();
                do {
                    container = capi.lookupContainer("testedRockets", spaceUri,
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            getRocketsTransaction);
                    rockets = capi.take(container,
                            FifoCoordinator.newSelector(1),
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            getRocketsTransaction);

                    rocket = rockets.get(0);
                    rocket.setPackerId(packerId);
                    rocket.setReadyForCollection(true);

                    if (rocket.getTestResult()) {
                        trashedRockets.add(rocket);
                    } else {
                        functioningRockets.add(rocket);
                    }
                } while (functioningRockets.size() < 5);

                container = capi.lookupContainer("finishedRockets",
                        spaceUri, MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction);
                capi.write(container, MzsConstants.RequestTimeout.TRY_ONCE,
                        getRocketsTransaction, new Entry(functioningRockets,
                                FifoCoordinator.newCoordinationData()));

                capi.commitTransaction(getRocketsTransaction);

                container = capi.lookupContainer("trashedRockets", spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null);
                for (Rocket trash : trashedRockets) {
                    capi.write(container, MzsConstants.RequestTimeout.TRY_ONCE,
                            null, new Entry(trash));
                }

            } catch (CountNotMetException e1) {
                LOGGER.info("Could not get all 5 rockets in time!");
                try {
                    capi.rollbackTransaction(getRocketsTransaction);
                } catch (MzsCoreException e2) {
                    LOGGER.error("Logistician can't rollback transaction!");
                    System.exit(1);
                }
            } catch (MzsCoreException e) {
                LOGGER.error("Logistician has problem with space!");
                System.exit(1);
            }
            try {
                container = capi.lookupContainer(
                        "trashedRockets",
                        spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null);
                ArrayList<Rocket> readRocket;
                readRocket = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                LOGGER.debug("Rockets in the trash: " + readRocket);

                container = capi.lookupContainer(
                        "finishedRockets",
                        spaceUri,
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null);
                readRocket = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                LOGGER.debug("Rockets in finished container " + readRocket);
            } catch (MzsCoreException e) {
                LOGGER.error("Logistician has problem with space!");
                System.exit(1);
            }
        }
    }
}
