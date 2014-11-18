package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import org.mozartspaces.core.*;
import org.slf4j.Logger;

import java.util.ArrayList;

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
        Wood wood = new Wood(1);
        Supplier supplier;
        for(int i =0; i < 10; i++){
            supplier = new Supplier(i);
            supplier.start();
        }


        try {
            woodContainer = capi.createContainer("Wood",
                    mozartSpace.getConfig().getSpaceUri(),
                    MzsConstants.Container.UNBOUNDED,
                    null,
                    null);
            capi.write(woodContainer, new Entry(wood));
            result = capi.read(woodContainer);
            LOGGER.debug("Read: " + result.toString());
            capi.destroyContainer(woodContainer, null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        mozartSpace.shutdown(true);
    }
}
