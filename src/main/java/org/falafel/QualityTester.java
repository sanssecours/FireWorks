package org.falafel;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents the qulity tester who decides if a rocket is defect or
 * not. The criteria for a defect rocket are if:
 *      more than one effect charge is faulty
 *      it contains less than 120g of the propellant charge
 */
public class QualityTester {

    /** Constant for the transaction timeout time. */
    private static final int TRANSACTION_TIMEOUT = 3000;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);

    /** Create the quality tester singleton. */
    private QualityTester() { }

    /**
     * Start the quality tester process.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {
        int testerId;
        Rocket rocket;
        ArrayList<Effect> effects;
        Random randomGenerator = new Random();
        int propellantQuantity;

        Capi capi;
        MzsCore core;
        URI spaceUri;
        TransactionReference getRocketsTransaction;
        
        if (arguments.length != 2) {
            System.err.println("Usage: QualityTester <Id> <Space URI>!");
            return;
        }

        try {
            testerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply a valid values!");
            return;
        }

        LOGGER.info("Quality tester " + testerId + " ready to test!");
    }
}
