package org.falafel;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.aspects.AbstractSpaceAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.CommitTransactionRequest;

import java.util.ArrayList;

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

        RequestContext context = request.getContext();

        if (context.containsProperty("gotMaterial")) {
            FireWorks.reduceCasingWood(1);

            FireWorks.changeEffectLabels(
                    (EffectColor) context.getProperty("color1") , -1);
            FireWorks.changeEffectLabels(
                    (EffectColor) context.getProperty("color2") , -1);
            FireWorks.changeEffectLabels(
                    (EffectColor) context.getProperty("color3") , -1);

            if (context.containsProperty("takenClosedPropellant")) {
                FireWorks.changeClosedPropellantLabels(-1);
            }
            if (context.containsProperty("takenOpenPropellant")) {
                int number = (int) context.getProperty("takenOpenPropellant");
                int quantity = (int) context.getProperty("takenOpenQuantity");

                FireWorks.changeOpenedPropellantLabels(-number, -quantity);
            }

        }

        return AspectResult.OK;
    }
}
