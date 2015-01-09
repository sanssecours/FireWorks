package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
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

import static org.falafel.MaterialType.Casing;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.mozartspaces.core.MzsConstants.RequestTimeout.TRY_ONCE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.falafel.MaterialType.Effect;
import static org.falafel.MaterialType.Propellant;
import static org.falafel.MaterialType.Wood;

/**
 * Thread to control the benchmark test.
 */
public class BenchmarkTest extends Thread {
    /** The resource identifier for the space. */
    private final URI spaceUri;
    /** Length of the Test in ms. */
    private static final int TEST_TIME = 60000;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(BenchmarkTest.class);

    /**
     * Create the benchmark test with the URI of the mozart space.
     *
     * @param spaceUri
     *          The resource identifier used to locate the space
     */
    public BenchmarkTest(final URI spaceUri) {
        this.spaceUri = spaceUri;
    }

    /**
     * Start the benchmark test.
     */
    public final void run() {
        ContainerReference container;
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);

        try {
            container = capi.lookupContainer("benchmark", spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
        } catch (MzsCoreException e) {
            LOGGER.error("Benchmark can't find the container!");
            return;
        }

        try {
            capi.write(container, MzsConstants.RequestTimeout.ZERO,
                    null, new Entry("Start"));
        } catch (MzsCoreException e) {
            LOGGER.error("Benchmark can't write to the container!");
            return;
        }

        LOGGER.error("Start of the Benchmark!");
        try {
            Thread.sleep(TEST_TIME);
        } catch (InterruptedException e) {
            System.out.println("Benchmark test sleep disturbed!");
        }

        try {
            capi.take(container,
                    AnyCoordinator.newSelector(),
                    MzsConstants.RequestTimeout.ZERO,
                    null);
        } catch (MzsCoreException e) {
            LOGGER.error("Benchmark can't take from the container!");
        }

        LOGGER.error("End of the Benchmark!");

        ContainerReference cont;
        ArrayList<Rocket> rockets;
        ArrayList<Serializable> entries;

        int numberRockets = 0;

        try {
            Thread.sleep(10000);

            cont = capi.lookupContainer("createdRockets",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            rockets = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Created Rockets: " + rockets.size());
            numberRockets += rockets.size();

            cont = capi.lookupContainer("testedRockets",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            rockets = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Tested Rockets: " + rockets.size());
            numberRockets += rockets.size();

            cont = capi.lookupContainer("trashedRockets",
                   spaceUri,
                   MzsConstants.RequestTimeout.TRY_ONCE, null);
            rockets = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Trashed Rockets: " + rockets.size());
            numberRockets += rockets.size();

            cont = capi.lookupContainer("finishedRockets",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            rockets = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Finished Rockets: " + 5 * rockets.size());
            numberRockets += 5 * rockets.size();

            cont = capi.lookupContainer("orderedRockets",
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            rockets = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Ordered Rockets: " + rockets.size());
            numberRockets += rockets.size();

            System.out.println("All rockets: " + numberRockets);

            cont = capi.lookupContainer(Casing.toString(),
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            entries = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Casings: " + entries.size());

            cont = capi.lookupContainer(Wood.toString(),
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            entries = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Wood: " + entries.size());

            cont = capi.lookupContainer(Propellant.toString(),
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            entries = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Propellants: " + entries.size());

            cont = capi.lookupContainer(Effect.toString(),
                    spaceUri,
                    MzsConstants.RequestTimeout.TRY_ONCE, null);
            entries = capi.read(cont,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            System.out.println("Effects: " + entries.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
