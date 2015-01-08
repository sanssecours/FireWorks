package org.falafel;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *  This class implements various aspects involving Materials.
 */
public class TestedRocketAspects extends AbstractContainerAspect {
    /** The counts how many rockets have been tested. */
    private static int benchmarkCounter = 0;
    /** Constant for how many rockets in the benchmark can be maximal tested.
     */
    private static final int MAX_NUMBER_BENCHMARK = 1500;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(NewRocketAspects.class);

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

        if (entries.get(0).getValue() instanceof Rocket) {
            for (Entry entry : entries) {
                Rocket rocket = (Rocket) entry.getValue();
                FireWorks.updateOfARocketInRocketsTable(rocket);
            }
        }
        benchmarkCounter += 1;
        if (benchmarkCounter == MAX_NUMBER_BENCHMARK) {
            LOGGER.error("1500 rockets tested by the Tester!!");
        }

        return AspectResult.OK;
    }
}
