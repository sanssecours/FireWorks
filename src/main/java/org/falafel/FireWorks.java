package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.mozartspaces.core.MzsConstants.Container;
import static org.slf4j.LoggerFactory.getLogger;

/* -- Class ---------------------------------------------------------------- */

/**
 * Main class for the project. This class provides an interface to start
 * suppliers and keep an eye on the progress of the production in the firework
 * factory.
 */
public final class FireWorks {

    /**
     * Get the Logger for the current class.
     */
    private static final Logger LOGGER = getLogger(FireWorks.class);

    /** Create the org.falafel.FireWorks singleton. */
    private FireWorks() { }


    /**
     * Start the firework factory.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {
        ContainerReference woodContainer;
        MzsCore mozartSpace = DefaultMzsCore.newInstance();
        Capi capi = new Capi(mozartSpace);

        ArrayList<Wood> result;
        Wood wood = new Wood(1337);
        Supplier supplier;
        final int numberOfSuppliers = 1;

        try {
            woodContainer = capi.createContainer("Wood",
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            capi.write(woodContainer, new Entry(wood));

            for (int supplierId = 1;
                 supplierId <= numberOfSuppliers;
                 supplierId++) {
                supplier = new Supplier(supplierId,
                        mozartSpace.getConfig().getSpaceUri());
                supplier.start();
            }

            TimeUnit.SECONDS.sleep(2);
            result = capi.read(woodContainer);
            LOGGER.debug("Read: " + result.toString());

            capi.destroyContainer(woodContainer, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mozartSpace.shutdown(true);
    }
}
