package org.falafel;

import java.io.Serializable;

/**
 * Created by Johannes on 18.11.2014.
 */
public class Material implements Serializable {

    private final int ID;

    public Material(int id){
        ID = id;
    }

    public int getID(){
        return ID;
    }

    public String toString(){
        return Integer.toString(ID);
    }
}
