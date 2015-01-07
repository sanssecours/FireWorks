package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;

import java.util.HashSet;
import java.util.Set;

/**
 *  This class implements an aspect that is called when the fireworks factory
 *  delivers rockets to a buyer.
 */
public class BuyerRocketsDeliveredAspect extends AbstractContainerAspect {

    /** Save the ids of all delivered purchases. */
    private static Set<Integer> purchases = new HashSet<>();

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
     *          The number of processings of this request
     * @return The aspect result
     */
    public final AspectResult postWrite(final WriteEntriesRequest request,
                                        final Transaction tx,
                                        final SubTransaction stx,
                                        final Capi3AspectPort capi3,
                                        final int executionCount) {

        Rocket rocket = (Rocket) request.getEntries().get(0).getValue();
        int purchaseId = rocket.getPurchase().getPurchaseId().intValue();

        if (!purchases.contains(purchaseId)) {
            Buyer.setPurchaseStatusToFinished(purchaseId);
            purchases.add(purchaseId);
        }

        return AspectResult.OK;
    }
}
