package org.falafel;

import org.mozartspaces.core.*;

/**
 * Created by Johannes on 18.11.2014.
 */
public class Supplier extends Thread {
    private final int ID;

    public Supplier (int id){
        super();
        ID = id;
    }

    public void run(){
        System.out.println("Supplier "+ ID + " active!");
        MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
        Capi capi = new Capi(core);

       // ContainerReference cref = capi.lookupContainer("");


        core.shutdown(true);
    }
}
