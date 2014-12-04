package org.falafel;

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionReference;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

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
    private static final Logger LOGGER = getLogger(Supplier.class);
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

        int functioningElements = (int) Math.ceil(
                order.getQuantity() * order.getQuality() / HUNDRED);
        boolean defect;

        ContainerReference container;
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);
        ArrayList<Material> result;
        Material newEntry;

        String orderType = order.getType();
        String orderSupplier = order.getSupplierName();
        String casing = MaterialType.Casing.toString();
        String effect = MaterialType.Effect.toString();
        String propellant = MaterialType.Propellant.toString();
        TransactionReference supplyTransaction;

        System.out.println("Supplier " + id + " active!");

        Random randomGenerator = new Random();

        for (int index = 0; index < order.getQuantity(); index++) {

            if (orderType.equals(casing)) {
                newEntry = new Casing(materialId, orderSupplier, id);
            } else if (orderType.equals(effect)) {
                defect = index >= functioningElements;
                newEntry = new Effect(materialId, orderSupplier, id, defect);
            } else if (orderType.equals(propellant)) {
                newEntry = new Propellant(materialId, orderSupplier, id,
                        Propellant.CLOSED);
            } else {
                newEntry = new Wood(materialId, orderSupplier, id);
            }

            int waitingTime = randomGenerator.nextInt(
                    UPPERBOUND - LOWERBOUND) + LOWERBOUND;
            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e) {
                LOGGER.error("I was interrupted while trying to sleep. "
                        + "How rude!");
            }

            try {
                supplyTransaction = capi.createTransaction(
                        TRANSACTIONTIMEOUT, spaceUri);
            } catch (MzsCoreException e) {
                LOGGER.debug("Can't create transaction!");
                return;
            }

            try {

                newEntry.setID(materialId + index);
                container = capi.lookupContainer(orderType, spaceUri,
                        RequestTimeout.TRY_ONCE, supplyTransaction);

                if (orderType.equals(propellant)) {
                    capi.write(container, RequestTimeout.ZERO,
                            supplyTransaction,
                            new Entry(newEntry,
                                      LindaCoordinator.newCoordinationData()));
                } else {
                    capi.write(container, RequestTimeout.ZERO,
                            supplyTransaction, new Entry(newEntry));
                }

                capi.commitTransaction(supplyTransaction);

                LOGGER.debug("Supplier " + id + " Wrote entry to container "
                        + orderType);
            } catch (MzsTimeoutException toe) {
                index--;
                LOGGER.debug("Can't write in container in time!");
            } catch (MzsCoreException e) {
                index--;
                try {
                    capi.rollbackTransaction(supplyTransaction);
                } catch (MzsCoreException e1) {
                    LOGGER.debug("Can't rollback transaction!");
                }
            }
        }

        core.shutdown(true);
    }
}
