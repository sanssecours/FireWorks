package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.aspects.AbstractSpaceAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;

import java.io.Serializable;
import java.util.List;

/**
 *  This class implements various aspects involving Materials.
 */
public class ReduceLabelSpaceAspects extends AbstractSpaceAspect {



    /**
     *
     * @param request
     *          The original request sent to the core.
     * @param tx
     *          The transaction, can be explicit or implicit
     * @return  The aspect result
     */
    public AspectResult postCommitTransaction(CommitTransactionRequest request,
                                              Transaction tx) {

        if (request.getContext().containsProperty("gotMaterial")) {
            FireWorks.reduceCasingEffectWood(1);
        }

        return AspectResult.OK;
    }
}
