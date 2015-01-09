package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.falafel.MaterialType.Casing;
import static org.falafel.MaterialType.Effect;
import static org.falafel.MaterialType.Propellant;
import static org.falafel.MaterialType.Wood;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.mozartspaces.core.MzsConstants.Container;
import static org.mozartspaces.core.MzsConstants.RequestTimeout.TRY_ONCE;
import static org.slf4j.LoggerFactory.getLogger;

/* -- Class ---------------------------------------------------------------- */

/**
 * Main class for the project. This class provides an interface to start
 * suppliers and keep an eye on the progress of the production in the firework
 * factory.
 */
public class FireWorks {

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);
    /** The space where we want to store our Material. */
    private static MzsCore mozartSpace;
    /** Reference to the API for the space. */
    private static Capi capi;
    /** The container for storing the casings. */
    private static ContainerReference casingContainer;
    /** The container for storing the effect charge supplies. */
    private static ContainerReference effectContainer;
    /** The container for storing the propellant charge supplies. */
    private static ContainerReference propellantContainer;
    /** The container for storing the wood supplies. */
    private static ContainerReference woodContainer;
    /** The container for storing the created rockets. */
    private static ContainerReference createdRockets;
    /** The container for storing the tested rockets. */
    private static ContainerReference testedRockets;
    /** The container for storing the packed rockets. */
    private static ContainerReference packedRockets;
    /** The container for storing the thrown out rockets. */
    private static ContainerReference wasteRockets;
    /** The container for storing the purchases. */
    private static ContainerReference purchaseContainer;
    /** The container for storing the finished ordered rockets. */
    private static ContainerReference orderedRocketsContainer;
    /** The container for starting and finishing the benchmark test. */
    private static ContainerReference benchmarkContainer;
    /** The running id for the suppliers. */
    private static int supplierId = 1;
    /** The running id for the materials. */
    private static int materialId = 1;

    /**  The data as an observable list for SupplyOrder. */
    private static ArrayList<SupplyOrder> order = new ArrayList<>();

    /** The URI for the space of the fireworks factory. */
    private static URI spaceURI;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    private static void initializeTest() {
        //CHECKSTYLE:OFF
        order.add(new SupplyOrder("Hulk", Casing.toString(), EffectColor.Blue,
                1500, 100));
        order.add(new SupplyOrder("Iron Man", Wood.toString(), EffectColor.Blue,
                1500, 100));
        order.add(new SupplyOrder("Captain America", Effect.toString(),
                EffectColor.Blue, 1500, 5));
        order.add(new SupplyOrder("Batman", Effect.toString(), EffectColor.Red,
                1500, 5));
        order.add(new SupplyOrder("Thor", Effect.toString(), EffectColor.Green,
                1500, 5));
        order.add(new SupplyOrder("Seaman", Propellant.toString(),
                EffectColor.Green, 500, 100));
        //CHECKSTYLE:ON
        startSuppliers();
    }

    /**
     * Start suppliers to fill the containers with Material.
     */
    private static void startSuppliers() {
        SupplyOrder nextOrder;
        Supplier supplier;

        while (!order.isEmpty()) {
            nextOrder = order.remove(0);
            LOGGER.debug(nextOrder.toString());
            supplier = new Supplier(supplierId,
                    mozartSpace.getConfig().getSpaceUri(), nextOrder,
                    materialId);
            supplier.start();
            supplierId++;
            materialId = materialId + nextOrder.getQuantity();
        }

        System.out.println("No new order!");
    }

    /**
     * Starts the benchmark test.
     */
    private static void startBenchmark() {
        BenchmarkTest test = new BenchmarkTest(spaceURI);
        test.start();
    }

    /**
     * Create the space and the core API.
     */
    private static void initSpace() {

        mozartSpace = DefaultMzsCore.newInstance();
        capi = new Capi(mozartSpace);

        ContainerAspect materialContainerAspect = new MaterialAspects();
        ContainerAspect newRocketContainerAspect = new NewRocketAspects();
        ContainerAspect testedRocketContainerAspect = new TestedRocketAspects();
        ContainerAspect packedRocketContainerAspect =
                new FinishedRocketAspects();
        ContainerAspect trashedRocketContainerAspect =
                new TrashedRocketAspects();
        ContainerAspect orderedRocketsContainerAspect =
                new OrderedRocketsAspects();
        spaceURI = mozartSpace.getConfig().getSpaceUri();
        Set<ContainerIPoint> iPoints = new HashSet<>();
        iPoints.add(ContainerIPoint.POST_WRITE);

        SpaceAspect aspect = new ReduceLabelSpaceAspects();
        Set<SpaceIPoint> p = new HashSet<>();
        p.add(SpaceIPoint.POST_COMMIT_TRANSACTION);
        try {
            capi.addSpaceAspect(aspect, spaceURI, p, null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }

        LOGGER.info("Space URI: " + spaceURI);

        try {
            // Create the supply containers
            casingContainer = capi.createContainer(
                    Casing.toString(),
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(materialContainerAspect, casingContainer,
                    iPoints, null);
            effectContainer = capi.createContainer(
                    Effect.toString(),
                    spaceURI,
                    Container.UNBOUNDED,
                    asList(new LindaCoordinator(), new AnyCoordinator()),
                    null,
                    null);
            capi.addContainerAspect(materialContainerAspect, effectContainer,
                    iPoints, null);
            propellantContainer = capi.createContainer(
                    Propellant.toString(),
                    spaceURI,
                    Container.UNBOUNDED,
                    asList(new LindaCoordinator(), new AnyCoordinator()),
                    null,
                    null);
            capi.addContainerAspect(materialContainerAspect,
                    propellantContainer, iPoints, null);
            woodContainer = capi.createContainer(
                    Wood.toString(),
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(materialContainerAspect, woodContainer,
                    iPoints, null);

            // create the container where the newly created rockets are stored
            createdRockets = capi.createContainer(
                    "createdRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(newRocketContainerAspect, createdRockets,
                    iPoints, null);
            // create the container where the tested rockets are stored with a
            // FiFo coordinator
            testedRockets = capi.createContainer(
                    "testedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    asList(new FifoCoordinator(), new AnyCoordinator()),
                    null,
                    null);
            capi.addContainerAspect(testedRocketContainerAspect, testedRockets,
                    iPoints, null);

            // create the container where the packed rockets are stored with a
            // FiFo coordinator
            packedRockets = capi.createContainer(
                    "finishedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    asList(new FifoCoordinator(), new AnyCoordinator()),
                    null,
                    null);
            capi.addContainerAspect(packedRocketContainerAspect, packedRockets,
                    iPoints, null);

            // create the container where the trashed rockets are stored
            wasteRockets = capi.createContainer(
                    "trashedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(trashedRocketContainerAspect, wasteRockets,
                    iPoints, null);

            // create the container where the purchases are stored
            purchaseContainer = capi.createContainer(
                    "purchase",
                    spaceURI,
                    Container.UNBOUNDED,
                    null);

            // create the container where the ordered rockets are stored
            orderedRocketsContainer = capi.createContainer(
                    "orderedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    asList(new LindaCoordinator(), new AnyCoordinator()),
                    null,
                    null);
            capi.addContainerAspect(orderedRocketsContainerAspect,
                    orderedRocketsContainer, iPoints, null);
            // create the container for the benchmark
            benchmarkContainer = capi.createContainer(
                    "benchmark",
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Close the containers and the space.
     */
    private static void closeSpace() {
        try {
            capi.destroyContainer(casingContainer, null);
            capi.destroyContainer(effectContainer, null);
            capi.destroyContainer(propellantContainer, null);
            capi.destroyContainer(woodContainer, null);
            capi.destroyContainer(createdRockets, null);
            capi.destroyContainer(testedRockets, null);
            capi.destroyContainer(packedRockets, null);
            capi.destroyContainer(wasteRockets, null);
            capi.destroyContainer(purchaseContainer, null);
            capi.destroyContainer(orderedRocketsContainer, null);
        } catch (MzsCoreException e) {
            LOGGER.error("Problems with destroying the containers "
                    + "in the space");
        }
        mozartSpace.shutdown(true);
        LOGGER.info("Closed space");
    }

     /**
     * Start the fireworks factory.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {
        initSpace();
        initializeTest();

        ContainerReference cont;
        ArrayList<Rocket> rockets;
        ArrayList<Serializable> entries;
        int materials = 0;

        try {

            while (materials < 8000) {
                materials = 0;
                cont = capi.lookupContainer(Casing.toString(),
                        spaceURI,
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                entries = capi.read(cont,
                        AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
                materials += entries.size();

                cont = capi.lookupContainer(Wood.toString(),
                        spaceURI,
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                entries = capi.read(cont,
                        AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
                materials += entries.size();

                cont = capi.lookupContainer(Propellant.toString(),
                        spaceURI,
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                entries = capi.read(cont,
                        AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
                materials += entries.size();

                cont = capi.lookupContainer(Effect.toString(),
                        spaceURI,
                        MzsConstants.RequestTimeout.TRY_ONCE, null);
                entries = capi.read(cont,
                        AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
                materials += entries.size();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        startBenchmark();
    }

}
