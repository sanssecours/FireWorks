package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;

/**
 * Created by Johannes on 21.11.2014.
 */
public class MaterialAspects extends AbstractContainerAspect {

    public AspectResult postWrite(WriteEntriesRequest request,
                                  Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
                                  int executionCount) {
        System.out.println("Wrote entries " + request.getEntries());
        return AspectResult.OK;
    }
}
