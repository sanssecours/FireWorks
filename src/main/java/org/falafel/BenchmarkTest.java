package org.falafel;

import java.net.URI;

/**
 * Thread to control the benchmark test.
 */
public class BenchmarkTest extends Thread {
    /** The resource identifier for the space. */
    private final URI spaceUri;
    /** Length of the Test in ms. */
    private static final int TEST_TIME = 60000;

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
        System.out.println("Start of the Benchmark!");
        try {
            Thread.sleep(TEST_TIME);
        } catch (InterruptedException e) {
            System.out.println("Benchmark test sleep disturbed!");
        }
        System.out.println("End of the Benchmark!");
    }
}
