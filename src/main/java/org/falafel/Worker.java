package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import java.net.URI;
import java.util.ArrayList;

import static org.mozartspaces.capi3.Selector.COUNT_ALL;

/**
 * .
 */
public class Worker {

    /** Constant for the transaction timeout time . */
    private static final int TRANSACTIONTIMEOUT = 5000;
    private static int workerId;

    private Worker() { };

    public static void main(final String[] arguments) {
        System.out.println("Please supply an ID!");
        if (arguments.length < 1) {
            System.out.println("Please supply an ID!");
            return;
        }
        try {
            workerId = Integer.parseInt(arguments[0]);
        } catch (NumberFormatException e) {
            System.out.println("Please supply a valid ID!");
            return;
        }


        ContainerReference casingContainer;
        ContainerReference effectContainer;
        ContainerReference propellantContainer;
        ContainerReference woodContainer;

        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);
        URI spaceUri = core.getConfig().getSpaceUri();

        ArrayList<Wood> result;

        TransactionReference collectResourcesTransaction;
        try {
            collectResourcesTransaction = capi.createTransaction(
                    TRANSACTIONTIMEOUT, spaceUri);
        } catch (MzsCoreException e) {
            e.printStackTrace();
            System.out.println("Can't create transaction!");
            return;
        }

        try {
            System.out.println("Transaction Happening!");
            casingContainer = capi.lookupContainer(
                    FireWorks.MaterialType.Casing.toString(), spaceUri,
                    RequestTimeout.TRY_ONCE, collectResourcesTransaction);
            result = capi.read(casingContainer,
                    AnyCoordinator.newSelector(COUNT_ALL),
                    RequestTimeout.TRY_ONCE, collectResourcesTransaction);

            System.out.println("Read: " + result.toString());
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }


    }
}
