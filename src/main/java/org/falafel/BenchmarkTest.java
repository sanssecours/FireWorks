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

import java.net.URI;

import static org.slf4j.LoggerFactory.getLogger;

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
    }
}
