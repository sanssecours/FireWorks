package org.falafel;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.aspects.AbstractSpaceAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.CommitTransactionRequest;

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
    public final AspectResult postCommitTransaction(
            final CommitTransactionRequest request, final Transaction tx) {

        if (request.getContext().containsProperty("gotMaterial")) {
            FireWorks.reduceCasingEffectWood(1);

            if (request.getContext().containsProperty("takenClosedPropellant"))
            {
                FireWorks.changeClosedPropellantLabels(-1);
            }
            if (request.getContext().containsProperty("takenOpenPropellant")) {
                int number = (int) request.getContext().getProperty(
                        "takenOpenPropellant");
                int quantity = (int) request.getContext().getProperty(
                        "takenOpenQuantity");

                FireWorks.changeOpenedPropellantLabels(-number, -quantity);
                System.out.println("Reduce number " + number
                        + " and difference " + quantity);
            }

        }

        return AspectResult.OK;
    }
}
