package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;

import java.io.Serializable;
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
     * @return The aspect result
     */
    public final AspectResult postWrite(final WriteEntriesRequest request,
                                        final Transaction tx,
                                        final SubTransaction stx,
                                        final Capi3AspectPort capi3,
                                        final int executionCount) {

        //System.out.println("container ID: " + request.getContainer().getId());
        List<Entry> entries = request.getEntries();

        if (entries.get(0).getValue() instanceof Propellant) {
            for (Entry entry : entries) {
                Propellant propellantEntry = (Propellant) entry.getValue();
                if (propellantEntry.getQuantity() == Propellant.FULL) {
                    FireWorks.changeCounterLabels(
                            request.getContainer().getId(), 1);
                } else {
                    FireWorks.changeOpenedPropellantLabels(
                            propellantEntry.getQuantity());
                }
            }
        } else {
            FireWorks.changeCounterLabels(request.getContainer().getId(),
                    request.getEntries().size());
        }
        return AspectResult.OK;
    }

    /**
     *
     * @param request
     *          The original request sent to the core.
     * @param tx
     *          The transaction, can be explicit or implicit
     * @param stx
     *          The sub-transaction for this operation
     * @param capi3
     *          The container-specific CAPI-3 interface
     * @param executionCount
     *          The number of processings of this request
     * @param entries
     *          The taken entries (result of the CAPI-3 operation)
     * @return  The aspect result
     */
    public final AspectResult postTake(final TakeEntriesRequest<?> request,
                                       final Transaction tx,
                                       final SubTransaction stx,
                                       final Capi3AspectPort capi3,
                                       final int executionCount,
                                       final List<Serializable> entries) {

        Material material = (Material) entries.get(0);

        if (material instanceof Propellant) {
            for (Serializable serializable : entries) {
                Propellant propellant = (Propellant) serializable;
                if (propellant.getQuantity() == Propellant.FULL) {
                    FireWorks.changeCounterLabels(
                            request.getContainer().getId(), -1);
                } else {
                    FireWorks.changeOpenedPropellantLabels(
                            -propellant.getQuantity());
                }
            }
        } else {
            FireWorks.changeCounterLabels(request.getContainer().getId(),
                    -entries.size());
        }

        return AspectResult.OK;
    }
}
