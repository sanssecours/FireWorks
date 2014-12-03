package org.falafel;

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

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a logistic worker.
 */
public final class Logistic {

    /**
     * Constant for the transaction timeout time.
     */
    private static final long TRANSACTION_TIMEOUT = 3000;
                                       // MzsConstants.RequestTimeout.INFINITE;
    /** Specifies how long a logistic worker waits until he tries to get
     *  new rockets after he was unable to get them the last time. */
    private static final int WAIT_TIME_LOGISTIC_MS = 2000;
    /** Constant for how many rockets are in one package. */
    private static final int PACKAGE_SIZE = 5;
    /** Collected functioning rockets. */
    private static ArrayList<Rocket> functioningRockets = new ArrayList<>();

    /**
     * Get the Logger for the current class.
     */
    private static final Logger LOGGER = getLogger(Logistic.class);
    /** The mozart spaces core. */
    private static MzsCore core;

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
        ArrayList<Rocket> rockets;
        Rocket rocket;
        Capi capi;
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

        ContainerReference rocketContainer;
        ContainerReference trashContainer;
        ContainerReference shippingContainer;


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
        } catch (MzsCoreException e) {
            LOGGER.error("Logistician can't find container!");
            return;
        }


        while (true) {
            try {

                rockets = capi.take(rocketContainer,
                        FifoCoordinator.newSelector(1),
                        MzsConstants.RequestTimeout.TRY_ONCE,
                        null);

                rocket = rockets.get(0);
                rocket.setPackerId(packerId);
                rocket.setReadyForCollection(true);

                if (rocket.getTestResult()) {
                    capi.write(trashContainer,
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            null, new Entry(rocket));
                } else {
                    functioningRockets.add(rocket);
                }

                if (functioningRockets.size() == PACKAGE_SIZE) {
                    capi.write(shippingContainer,
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            null, new Entry(functioningRockets,
                                    FifoCoordinator.newCoordinationData()));
                    functioningRockets.clear();
                }

            } catch (CountNotMetException e1) {
                LOGGER.info("Could not get all 5 rockets in time!");

                for (Rocket returnRocket : functioningRockets) {
                    returnRocket.setPackageId(0);
                    returnRocket.setReadyForCollection(false);
                    try {
                        capi.write(rocketContainer,
                                MzsConstants.RequestTimeout.TRY_ONCE,
                                null, new Entry(returnRocket));
                    } catch (MzsCoreException e) {
                        LOGGER.error("Logistician can't return rockets to "
                                + "tested container!");
                        System.exit(1);
                    }
                    functioningRockets.clear();
                }
                try {
                    Thread.sleep(WAIT_TIME_LOGISTIC_MS);
                } catch (InterruptedException e) {
                    LOGGER.error("I was interrupted while trying to sleep. "
                            + "How rude!");
                }
            } catch (MzsCoreException e) {
                LOGGER.error("Logistician has problem with space!");
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
                close();
            }
        });
    }

    /**
     * Shutting down the logistic worker.
     */
    private static void close()	{
        System.out.println("I'm going home.");
        core.shutdown(true);
    }
}
