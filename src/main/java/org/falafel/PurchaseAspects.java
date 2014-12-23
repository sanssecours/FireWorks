package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;

import java.net.URI;
import java.util.List;

/**
 *  This class implements various aspects involving Materials.
 */
public class PurchaseAspects extends AbstractContainerAspect {

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

        List<Entry> entries = request.getEntries();

        RequestContext context = request.getContext();
        URI spaceUri = request.getContainer().getSpace();
        Purchase purchase = (Purchase) entries.get(0).getValue();

        if (context != null) {
            if (context.containsProperty("newPurchase")) {
                // Start thread to write the purchases in the container
                WritePurchasesToContainer writer
                        = new WritePurchasesToContainer(spaceUri, purchase);
                writer.start();

                FireWorks.addPurchaseToTable(purchase);
            }
        }
        return AspectResult.OK;
    }
}
