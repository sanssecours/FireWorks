package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.operation.DefaultContainer;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import java.util.List;

/**
 *  This class implements various aspects involving Materials.
 */
public class MaterialAspects extends AbstractContainerAspect {

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
     * @return the aspect result
     */
    public final AspectResult postWrite(final WriteEntriesRequest request,
                                        final Transaction tx,
                                        final SubTransaction stx,
                                        final Capi3AspectPort capi3,
                                        final int executionCount) {

        //System.out.println("container ID: " + request.getContainer().getId());
        List<Entry> entries = request.getEntries();
        for (int index = 0; index < entries.size(); index++) {
            FireWorks.changeCounterLabels(request.getContainer().getId(),
                    request.getEntries().size());
        }

        return AspectResult.OK;
    }
}
