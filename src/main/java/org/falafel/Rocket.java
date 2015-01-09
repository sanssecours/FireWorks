package org.falafel;

import org.mozartspaces.capi3.Queryable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * .
 */
@Queryable
public class Rocket implements Serializable {

    /** The different classes of the rocket. */
    public enum QualityClass { A, B, NotSet, Bad }
    /** The identification of this rocket. */
    private Integer id;
    /** The id of the package that contains this rocket. */
    private Integer packageId = 0;
    /** The wood used to construct this rocket. */
    private Wood wood;
    /** The casing used to construct this rocket. */
    private Casing casing;
    /** The list of effects used to construct this rocket. */
    private ArrayList<Effect> effects;
    /** The propellants together with the amounts of them used to create this
     *  rocket. */
    private HashMap<Propellant, Integer> propellants;
    /** The whole amount of propellant used to create this rocket. */
    private Integer propellantQuantity;
    /** The id of the worker that put together this rocket. */
    private Integer workerId;
    /** The id of the tester that checked this rocket. */
    private Integer testerId = 0;
    /** The id of the logistician that boxed this rocket. */
    private Integer packerId = 0;
    /** This value specifies if this rocket is defect or not. */
    //private Boolean testResult = false;
    /** The id of the purchase order. */
    /** The class of the rocket. */
    private QualityClass qualityClass = QualityClass.NotSet;

    /**
     * Create a new rocket with the given arguments.
     *
     * @param rocketId
     *          The identifier for the rocket
     * @param wood
     *          The wood used to construct the new rocket
     * @param casing
     *          The casing used to construct the new rocket
     * @param effects
     *          A list of effects used to create the new rocket
     * @param propellants
     *          The propellants used to create the new rocket
     * @param propellantQuantity
     *          The amount of propellant contained in the new rocket
     * @param workerId
     *          The id of the worker that created the new rocket
     */
    public Rocket(final Integer rocketId,
                    final Wood wood,
                    final Casing casing,
                    final ArrayList<Effect> effects,
                    final HashMap<Propellant, Integer> propellants,
                    final Integer propellantQuantity,
                    final Integer workerId) {
        id = rocketId;
        this.wood = wood;
        this.casing = casing;
        this.effects = effects;
        this.propellants = propellants;
        this.propellantQuantity = propellantQuantity;
        this.workerId = workerId;
    }

    /**
     * Set the id of the Rocket.
     *
     * @param newId integer value which is set for the rocket id
     */
    public final void setNewRocketId(final Integer newId) {
        id = newId;
    }
    /**
     * returns the id of the rocket.
     *
     * @return the id of the rocket
     */
    public final Integer getRocketId() {
        return id;
    }
    /**
     * Return the effect charges of the rocket.
     *
     * @return the array list with the effect charger
     */
    public final ArrayList<Effect> getEffects() {
        return effects;
    }
    /**
     * Set the result of quality test to class A.
     */
    public final void setQualityClassA() {
        qualityClass = QualityClass.A;
    }
    /**
     * Set the result of quality test to class B.
     */
    public final void setQualityClassB() {
        qualityClass = QualityClass.B;
    }
    /**
     * Set the result of quality test to class B.
     */
    public final void setQualityClassBad() {
        qualityClass = QualityClass.Bad;
    }
    /**
     * Return the quantity of the propellant charges of the rocket.
     *
     * @return the integer value of the propellant charge
     */
    public final Integer getPropellantQuantity() {
        return propellantQuantity;
    }
    /**
     * returning the result of the quality test.
     *
     * @return boolean value of the test result
     */
    public final QualityClass getTestResult() {
        return qualityClass;
    }
    /**
     * sets the id of the quality tester who tested the rocket.
     *
     * @param testerId of the quality tester
     */
    public final void setTester(final int testerId) {
        this.testerId = testerId;
    }
    /**
     * Set the id of the worker who packed the rocket in logistics.
     *
     * @param packerId the id for the packer
     */
    public final void setPackerId(final Integer packerId) {
        this.packerId = packerId;
    }
    /**
     * Set the id of the package containing the rocket.
     *
     * @param id of the package
     */
    public final void setPackageId(final Integer id) {
        packageId = id;
    }

    /**
     * Return the string representation of the rocket.
     *
     * @return A string containing the rocket
     */
    public final String toString() {
        return "Rocket Id: " + id;
    }

}
