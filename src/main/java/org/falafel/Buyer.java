package org.falafel;

import java.net.URI;

/**
 * This class represents a buyer of rockets.
 *
 * A buyer orders rockets from the factory and stores them into his space after
 * the rockets were produced.
 */
public final class Buyer {

    /** Create a new empty buyer. */
    private Buyer() { }

    /**
     * Start the buyer.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {

        Integer buyerId;
        URI spaceUri;

        if (arguments.length != 2) {
            System.err.println("Usage: buyer <Id> <Space URI>");
            return;
        }
        try {
            buyerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply valid command line arguments!");
            return;
        }

        System.out.println("Id:  " + buyerId + "\nURI: " + spaceUri);
    }

}
