package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;

import java.util.HashMap;
import java.util.List;

/**
 * This class implements various aspects involving the orderedRockets container.
 */
public class OrderedRocketsAspects extends AbstractContainerAspect {

    /** The rocket counters for the different purchases. */
    private static HashMap<Integer, HashMap<Integer, Integer>> purchaseCounters
            = new HashMap<>();
    /**
     * This aspect will be called after an entry is written to an container.
     *
     * @param request
     *          The original request sent to the core
     * @param tx
     *          The transaction, can be explicit or implicit
     * @param stx
     *          The sub-transaction for this operation
     * @param capi3
     *          The container-specific CAPI-3 interface
     * @param executionCount
     *          The number of processing of this request
     * @return The aspect result
     */
    public final AspectResult postWrite(final WriteEntriesRequest request,
                                        final Transaction tx,
                                        final SubTransaction stx,
                                        final Capi3AspectPort capi3,
                                        final int executionCount) {

        List<Entry> entries = request.getEntries();
        Rocket rocket = (Rocket) entries.get(0).getValue();
        Purchase purchase = rocket.getPurchase();
        Integer buyerId = purchase.getBuyerId().intValue();
        Integer purchaseId = purchase.getPurchaseId().intValue();

        if (purchaseCounters.containsKey(buyerId)) {
            if (purchaseCounters.get(buyerId).containsKey(purchaseId)) {
                Integer counter = purchaseCounters.get(buyerId).get(purchaseId);
                purchaseCounters.get(buyerId).put(purchaseId, counter + 1);
            } else {
                purchaseCounters.get(buyerId).put(purchaseId, 1);
            }
        } else {
            HashMap<Integer, Integer> newBuyersPurchase = new HashMap<>();
            newBuyersPurchase.put(purchaseId, 1);
            purchaseCounters.put(buyerId, newBuyersPurchase);
        }

        System.out.println("current counter" + purchaseCounters);
        return AspectResult.OK;
    }
}
