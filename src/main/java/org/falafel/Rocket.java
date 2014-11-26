package org.falafel;

import java.util.ArrayList;

/**
 * .
 */
public class Rocket {

    Integer id;
    Wood wood;
    Casing casing;
    ArrayList<Effect> effects;
    ArrayList<Propellant> propellants;
    Integer propellantQuantity;
    Integer workerId;
    Integer testerId;
    Boolean testResult;
    Boolean readyForCollection;
    Boolean packageId;

    public Rocket (Integer rocketId,
                   Wood wood,
                   Casing casing,
                   ArrayList<Effect> effects,
                   ArrayList<Propellant> propellants,
                   Integer propellantQuantity,
                   Integer workerId){
        id = rocketId;
        this.wood = wood;
        this.casing = casing;
        this.effects = effects;
        this.propellants = propellants;
        this.propellantQuantity = propellantQuantity;
        this.workerId = workerId;
    }
}
