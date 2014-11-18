package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
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
        ContainerReference wood;
        MzsCore mozartSpace = DefaultMzsCore.newInstance();
        Capi capi = new Capi(mozartSpace);
        ArrayList<String> result;

        try {
            wood = capi.createContainer();
            capi.write(wood, new Entry("Test"));
            result = capi.read(wood);
            LOGGER.debug("Read: " + result.toString());
            capi.destroyContainer(wood, null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        mozartSpace.shutdown(true);
    }
}
