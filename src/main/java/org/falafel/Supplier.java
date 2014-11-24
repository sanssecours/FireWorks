package org.falafel;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class Supplier extends Thread {

    /** Constant for the lower bound of the loading time per element. */
    private static final int LOWERBOUND = 1000;
    /** Constant for the upper bound of the loading time per element. */
    private static final int UPPERBOUND = 2000;
    /** Constant for the transaction timeout time. */
    private static final int TRANSACTIONTIMEOUT = 3000;
    /** Constant for the division by 100. */
    private static final double HUNDRED = 100.0;
    /** Save the (unique) identifier for this supplier. */
    private final int id;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);
    /** The resource identifier for the space. */
    private final URI spaceUri;
    /** The order which the supplier shipped. */
    private final SupplyOrder order;
    /** Save the (unique) identifier for the materials in this order. */
    private final int materialId;

    /**
     * Create a new Supplier with a given id.
     *
     * @param identifier
     *          The (unique) identifier for this supplier
     * @param space
     *          The resource identifier used to locate the space
     * @param order
     *          The order the supplier should provide
     * @param startId
     *          Saves the first identifier of the ids that should be used for
     *          the materials in the order.
     */
    public Supplier(final int identifier, final URI space,
                    final SupplyOrder order, final int startId) {
        super();
        id = identifier;
        spaceUri = space;
        this.order = order;
        materialId = startId;
    }

    /**
     * Start the supplier.
     */
    public final void run() {
        ContainerReference container;
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);
        ArrayList<Wood> result;
        Material newEntry;
        int functioningElements = (int) Math.ceil(
                order.getQuantity() * order.getQuality() / HUNDRED);

        System.out.println("Supplier " + id + " active!");

        Random randomGenerator = new Random();

        for (int index = 0; index < order.getQuantity(); index++) {
            if (order.getType().equals(
                    FireWorks.MaterialType.Casing.toString())) {
                newEntry = new Casing(materialId, order.getSupplierName(), id);
            } else if (order.getType().equals(
                    FireWorks.MaterialType.Effect.toString())) {
                if (index < functioningElements) {
                    newEntry = new Effect(materialId, order.getSupplierName(),
                            id, false);
                } else {
                    newEntry = new Effect(materialId, order.getSupplierName(),
                            id, true);
                }
            } else if (order.getType().equals(
                    FireWorks.MaterialType.Propellant.toString())) {
                newEntry = new Propellant(materialId, order.getSupplierName(),
                        id, Propellant.CLOSED);
            } else {
                newEntry = new Wood(materialId, order.getSupplierName(), id);
            }

            TransactionReference supplyTransaction;
            try {
                supplyTransaction = capi.createTransaction(
                        TRANSACTIONTIMEOUT, spaceUri);
            } catch (MzsCoreException e) {
                e.printStackTrace();
                return;
            }
            try {
                int waitingTime = randomGenerator.nextInt(
                        UPPERBOUND - LOWERBOUND) + LOWERBOUND;
//                System.out.println("Waiting Time: " + waitingTime);
                Thread.sleep(waitingTime);

                newEntry.setID(materialId + index);
                container = capi.lookupContainer(order.getType(), spaceUri,
                        RequestTimeout.TRY_ONCE, supplyTransaction);

                capi.write(container, RequestTimeout.ZERO, supplyTransaction,
                        new Entry(newEntry));

                LOGGER.debug("Supplier " + id + " Wrote entry to container "
                        + order.getType());
                result = capi.read(container,
                        AnyCoordinator.newSelector(COUNT_ALL),
                        RequestTimeout.TRY_ONCE, supplyTransaction);
                LOGGER.debug("Supplier " + id + " Read: " + result.toString());
                capi.commitTransaction(supplyTransaction);
            } catch (MzsCoreException e) {
                e.printStackTrace();
                index--;
                try {
                    capi.rollbackTransaction(supplyTransaction);
                } catch (MzsCoreException e1) {
                    e1.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        core.shutdown(true);
    }
}
