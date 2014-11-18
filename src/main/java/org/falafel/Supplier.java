package org.falafel;

import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;

/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class Supplier extends Thread {

    /**
     * Save the (unique) identifier for this supplier.
     */
    private final int id;


    /**
     * Create a new Supplier with a given id.
     *
     * @param identifier
     *          The (unique) identifier for this supplier
     */
    public Supplier(final int identifier) {
        super();
        id = identifier;
    }

    /**
     * Start the supplier.
     */
    public final void run() {
        System.out.println("Supplier " + id + " active!");
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        //Capi capi = new Capi(core);
        // ContainerReference cref = capi.lookupContainer("");

        core.shutdown(true);
    }
}
