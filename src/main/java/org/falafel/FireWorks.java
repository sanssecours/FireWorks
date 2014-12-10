package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.falafel.MaterialType.Casing;
import static org.falafel.MaterialType.Effect;
import static org.falafel.MaterialType.Propellant;
import static org.falafel.MaterialType.Wood;
import static org.mozartspaces.core.MzsConstants.Container;
import static org.slf4j.LoggerFactory.getLogger;

/* -- Class ---------------------------------------------------------------- */

/**
 * Main class for the project. This class provides an interface to start
 * suppliers and keep an eye on the progress of the production in the firework
 * factory.
 */
public class FireWorks extends Application {

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
    /** The running id for the suppliers. */
    private static int supplierId = 1;
    /** The running id for the materials. */
    private static int materialId = 1;

    /**  The data as an observable list for SupplyOrder. */
    private static ObservableList<SupplyOrder> order =
            FXCollections.observableArrayList();

    /**  The data as an observable list for rockets. */
    private static ObservableList<Rocket> rockets =
            FXCollections.observableArrayList();
    /**  The data as an observable list for the trashed rockets. */
    private static ObservableList<Rocket> trashedRocketsList =
            FXCollections.observableArrayList();
    /** The data as an observable list for the rockets which are already packed.
     * */
    private static ObservableList<Rocket> packedRocketsList =
            FXCollections.observableArrayList();

    /** Specify the different choices a supplier can provide. */
    private static final ObservableList<String> TYPES_CHOICE_LIST =
            FXCollections.observableArrayList(Casing.toString(),
                    Effect.toString(),
                    Propellant.toString(),
                    Wood.toString());

    /** Save data shown in the rocket table. */
    @FXML
    private TableColumn<Rocket, String> effectIdColumn, propellantIdColumn,
            testResultColumn,  supplierPropellantIdColumn,
            supplierEffectIdColumn;
    /** Save data shown in the rocket table. */
    @FXML
    private TableColumn<Rocket, Number> rocketIdColumn, casingIdColumn,
            packageIdColumn, woodIdColumn, propellantQuantityColumn,
            workerIdColumn, testerIdColumn, supplierWoodIdColumn,
            supplierCasingIdColumn, packerIdColumn;
    /** Save handler of the rocket table. */
    @FXML
    private TableView<Rocket> rocketTable = new TableView<>();

    /** Saves data shown in the supplier table. */
    @FXML
    private TableView<SupplyOrder> supplyTable;
    /** Saves data shown in the name column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> supplierNameColumn;
    /** Saves data shown in the type column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedTypeColumn;
    /** Saves data shown in the quantity column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedQuantityColumn;
    /** Saves data shown in the quality column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedQualityColumn;
    /** Label for the current number of elements in the effect container. */
    @FXML
    private Label effectCounterLabel;
    /** Saves data shown in the effectCounterLabel. */
    private static Integer effectCounter = 0;
    /** Saves data shown in the effectCounterLabel. */
    private static StringProperty effectCounterProperty =
            new SimpleStringProperty(effectCounter.toString());
    /** Label for the current number of elements in the casing container. */
    @FXML
    private Label casingsCounterLabel;
    /** Saves data shown in the casingsCounterLabel. */
    private static Integer casingsCounter = 0;
    /** Saves data shown in the casingsCounterLabel. */
    private static StringProperty casingsCounterProperty =
            new SimpleStringProperty(casingsCounter.toString());
    /** Label for the current number of elements in the wood container. */
    @FXML
    private Label woodCounterLabel;
    /** Saves data shown in the woodCounterLabel. */
    private static Integer woodCounter = 0;
    /** Saves data shown in the woodCounterLabel. */
    private static StringProperty woodCounterProperty =
            new SimpleStringProperty(woodCounter.toString());
    /** Label for the current number of elements in the propellant container. */
    @FXML
    private Label propellantCounterLabel;
    /** Saves data shown in the propellantCounterLabel. */
    private static Integer propellantCounter = 0;
    /** Saves data shown in the propellantCounterLabel. */
    private static StringProperty propellantCounterProperty =
            new SimpleStringProperty(propellantCounter.toString());

    /** Label for the current number of open propellant charges in the
     * container. */
    @FXML
    private Label numberOpenPropellantLabel;
    /** Saves data shown in the numberOpenPropellant. */
    private static Integer numberOpenPropellantCounter = 0;
    /** Saves data shown in the numberOpenPropellant. */
    private static StringProperty numberOpenPropellantCounterProperty =
            new SimpleStringProperty(numberOpenPropellantCounter.toString());
    /** Label for the current quantity in grams of open propellant charges in
     * the container. */
    @FXML
    private Label quantityOpenPropellantLabel;
    /** Saves data shown in the quantityOpenPropellant. */
    private static Integer quantityOpenPropellantCounter = 0;
    /** Saves data shown in the quantityOpenPropellant. */
    private static StringProperty quantityOpenPropellantCounterProperty =
            new SimpleStringProperty(quantityOpenPropellantCounter.toString());

    /** Displays the number of rockets in the current rocket table. */
    @FXML
    private Label numberRocketsLabel;
    /** The number of produced rockets. */
    @FXML
    private static StringProperty numberRocketsProperty =
            new SimpleStringProperty("0");
    /** The number of shipped rockets. */
    @FXML
    private static StringProperty numberShippedRocketsProperty =
            new SimpleStringProperty("0");
    /** The number of trashed rockets. */
    @FXML
    private static StringProperty numberTrashedRocketsProperty =
            new SimpleStringProperty("0");

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // initialize rocket table
        rocketIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getIdProperty());
        packageIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPackageIdProperty());
        casingIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getCasingIdProperty());
        propellantIdColumn.setCellValueFactory(
                cellData
                      -> cellData.getValue().getPropellantPackageIdProperty());
        woodIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getWoodIdProperty());
        effectIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getEffectIdProperty());
        propellantQuantityColumn.setCellValueFactory(
                cellData
                       -> cellData.getValue().getPropellantQuantityProperty());
        testResultColumn.setCellValueFactory(
                cellData -> cellData.getValue().getTestResultProperty());
        workerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getWorkerIdProperty());
        testerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getTesterIdProperty());
        packerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPackerIdProperty());
        supplierWoodIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierWoodIdProperty());
        supplierCasingIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierCasingIdProperty());
        supplierPropellantIdColumn.setCellValueFactory(
                cellData
                     -> cellData.getValue().getSupplierPropellantIdProperty());
        supplierEffectIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierEffectIdProperty());

        // initialize current warehouse labels
        casingsCounterLabel.textProperty().bind(casingsCounterProperty);
        effectCounterLabel.textProperty().bind(effectCounterProperty);
        propellantCounterLabel.textProperty().bind(propellantCounterProperty);
        woodCounterLabel.textProperty().bind(woodCounterProperty);
        numberOpenPropellantLabel.textProperty().bind(
                numberOpenPropellantCounterProperty);
        quantityOpenPropellantLabel.textProperty().bind(
                quantityOpenPropellantCounterProperty);
        numberRocketsLabel.textProperty().bind(numberRocketsProperty);

        //  initialize supplier table
        supplierNameColumn.setCellValueFactory(
                cellData -> cellData.getValue().supplierNameProperty());
        supplierNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        supplierNameColumn.isEditable();

        orderedTypeColumn.setCellValueFactory(
                cellData -> cellData.getValue().typeProperty());
        orderedTypeColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(TYPES_CHOICE_LIST));
        orderedTypeColumn.isEditable();

        orderedQuantityColumn.setCellValueFactory(
                cellData -> cellData.getValue().quantityProperty());
        orderedQuantityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());
        orderedQuantityColumn.isEditable();

        orderedQualityColumn.setCellValueFactory(
                cellData -> cellData.getValue().qualityProperty());
        orderedQualityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());
        orderedQualityColumn.isEditable();

        //CHECKSTYLE:OFF
        order.add(new SupplyOrder("Hulk", Casing.toString(), 150, 100));
        order.add(new SupplyOrder("Iron Man", Wood.toString(), 150, 100));
        order.add(new SupplyOrder("Captain America", Effect.toString(), 150,
                  100));
        order.add(new SupplyOrder("Batman", Effect.toString(), 150, 60));
        order.add(new SupplyOrder("Thor", Effect.toString(), 150, 60));
        order.add(new SupplyOrder("Seaman", Propellant.toString(), 50, 100));
        order.add(new SupplyOrder("Hawk", Propellant.toString(), 50, 100));
        //CHECKSTYLE:ON

        supplyTable.isEditable();
        supplyTable.setItems(order);

        rocketTable.setItems(rockets);
        numberRocketsProperty.set(Integer.toString(rockets.size()));
    }

    /**
     * The faulty rocket is displayed in the trash table.
     *
     * @param rocket which is trashed
     */
    public static void addRocketToTrash(final Rocket rocket) {
        Platform.runLater(() -> {
            trashedRocketsList.add(rocket);
            numberTrashedRocketsProperty.set(Integer.toString(
                    trashedRocketsList.size()));
        });
    }

    /**
     * Add packed rockets to the shipping list.
     *
     * @param rockets rockets which are packed for shipping
     */
    public static void addRocketsToFinishedContainer(
                final ArrayList<Rocket> rockets) {
        Platform.runLater(() -> {
            packedRocketsList.addAll(rockets);
            numberShippedRocketsProperty.set(Integer.toString(
                    packedRocketsList.size()));
        });
    }

    /**
     *
     *
     * @param containerId defines which rocket table (created/tested, packed
     *                    or thrown away) should be updated
     * @param rocket the rocket to add to the container
     */
    public static void addNewRocketToTable(final String containerId,
                                           final Rocket rocket) {
        Platform.runLater(() -> {
            if (containerId.equals(createdRockets.getId())) {
                rockets.add(rocket);
                numberRocketsProperty.set(Integer.toString(rockets.size()));
            }
        });
    }

    /**
     * Updates the tested result of a rocket in the rocket table.
     *
     * @param updatedRocket which has been tested
     */
    public static void updateOfARocketInRocketsTable(
            final Rocket updatedRocket) {
        Platform.runLater(() -> {
            for (int index = 0; index < rockets.size(); index++) {
                Rocket rocket = rockets.get(index);
                int id = rocket.getRocketId();
                int newId = updatedRocket.getRocketId();
                if (id == newId) {
                    rockets.set(index, updatedRocket);
                    break;
                }
            }
        });
    }
    /**
     * Updates the counters in the GUI.
     *
     * @param number
     *          The value that should be added or subtracted number of opened
     *          propellants.
     * @param difference
     *          The value that should be added or subtracted from the current
     *          quantity (in grams).
     *
     */
    public static void changeOpenedPropellantLabels(final int number,
                                                    final int difference) {
        Platform.runLater(() -> {
            numberOpenPropellantCounter = numberOpenPropellantCounter + number;
            quantityOpenPropellantCounter = quantityOpenPropellantCounter
                    + difference;
            numberOpenPropellantCounterProperty.set(
                    numberOpenPropellantCounter.toString());
            quantityOpenPropellantCounterProperty.set(
                    quantityOpenPropellantCounter.toString());
        });
    }
    /**
     * Updates the counters in the GUI.
     *
     * @param difference
     *          The value that should be added or subtracted from the current
     *          quantity (in grams).
     *
     */
    public static void changeClosedPropellantLabels(final int difference) {
        Platform.runLater(() -> {
            propellantCounter = propellantCounter + difference;
            propellantCounterProperty.set(
                    propellantCounter.toString());
        });
    }

    /**
     * Updates the counters in the GUI.
     *
     * @param difference
     *          The value that should be subtracted from the Casing, Effect
     *          and Wood counter in the GUI
     *
     */
    public static void reduceCasingEffectWood(final int difference) {
        Platform.runLater(() -> {
            casingsCounter = casingsCounter - difference;
            casingsCounterProperty.set(casingsCounter.toString());
            effectCounter = effectCounter
                    - difference - difference - difference;
            effectCounterProperty.set(effectCounter.toString());
            woodCounter = woodCounter - difference;
            woodCounterProperty.set(woodCounter.toString());
        });
    }

    /**
     * Updates the counters in the GUI.
     *
     * @param containerId
     *          ID of the changed container
     * @param difference
     *          The value that should be added or subtracted from the element
     *          with the identifier {@code containerId}.
     *
     */
    public static void changeCounterLabels(final String containerId,
                                           final int difference) {
        Platform.runLater(() -> {
            if (containerId.equals(casingContainer.getId())) {
                casingsCounter = casingsCounter + difference;
                casingsCounterProperty.set(casingsCounter.toString());
            }
            if (containerId.equals(effectContainer.getId())) {
                effectCounter = effectCounter + difference;
                effectCounterProperty.set(effectCounter.toString());
            }
            if (containerId.equals(propellantContainer.getId())) {
                propellantCounter = propellantCounter + difference;
                propellantCounterProperty.set(
                        propellantCounter.toString());
            }
            if (containerId.equals(woodContainer.getId())) {
                woodCounter = woodCounter + difference;
                woodCounterProperty.set(woodCounter.toString());
            }
        });
    }

    /**
     * Start suppliers to fill the containers with Material.
     *
     * @param event
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     *
     */
    @FXML
    private void startSuppliers(final ActionEvent event) {
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
     * This method will be invoked when we create a new order.
     *
     * @param actionEvent
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void newOrder(final ActionEvent actionEvent) {
        order.add(new SupplyOrder());
    }

    /**
     * This method will be called when the shipped tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void displayShippedRocketsTab(final Event event) {
        rocketTable.setItems(packedRocketsList);
        numberRocketsLabel.textProperty().bind(numberShippedRocketsProperty);
    }

    /**
     * This method will be called when the trashed tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void displayTrashedRocketsTab(final Event event) {
        rocketTable.setItems(trashedRocketsList);
        numberRocketsLabel.textProperty().bind(numberTrashedRocketsProperty);
    }

    /**
     * This method will be called when the produced tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void displayProducedRocketsTab(final Event event) {
        rocketTable.setItems(rockets);
        numberRocketsLabel.textProperty().bind(numberRocketsProperty);
    }

    /**
     * This method will be invoked when a new supplier name is set.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setSupplierName(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setSupplierName(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when a new type for the material is set.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setType(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setType(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when a new quantity for a material is set.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setQuantity(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        SupplyOrder newValue = stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow());
        try {
            newValue.setQuantity(Integer.parseInt(
                    stCellEditEvent.getNewValue()));
        } catch (NumberFormatException e) {
            System.out.println("Not a number!");
        }
    }

    /**
     * This method will be invoked when the quality of a material is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setQuality(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        SupplyOrder newValue = stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow());
        try {
            newValue.setQuality(Integer.parseInt(
                    stCellEditEvent.getNewValue()));
        } catch (NumberFormatException e) {
            System.out.println("Not a number!");
        }

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
        URI spaceURI = mozartSpace.getConfig().getSpaceUri();
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
            /* The container for storing the tested rockets. */
            ContainerReference testedRockets = capi.createContainer(
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
            /* The container for storing the packed rockets. */
            ContainerReference packedRockets = capi.createContainer(
                    "finishedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    asList(new FifoCoordinator(), new AnyCoordinator()),
                    null,
                    null);
            capi.addContainerAspect(packedRocketContainerAspect, packedRockets,
                    iPoints, null);

            // create the container where the trashed rockets are stored
            /* The container for storing the thrown out rockets. */
            ContainerReference wasteRockets = capi.createContainer(
                    "trashedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(trashedRocketContainerAspect, wasteRockets,
                    iPoints, null);
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
        } catch (MzsCoreException e) {
            e.printStackTrace();
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
        launch(arguments);
    }

    @Override
    public final void start(final Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/FireWorks.fxml"));

        primaryStage.setTitle("Fireworks Factory");
        primaryStage.setOnCloseRequest(event -> closeSpace());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Remove all suppliers from the supplier table.
     *
     * @param actionEvent
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     *
     */
    @SuppressWarnings("unused")
    public final void clearOrder(final ActionEvent actionEvent) {
        order.clear();
    }

}
