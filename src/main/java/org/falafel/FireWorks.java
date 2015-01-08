package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.RequestContext;
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
    /** The running id for the suppliers. */
    private static int supplierId = 1;
    /** The running id for the materials. */
    private static int materialId = 1;

    /**  The data as an observable list for SupplyOrder. */
    private static ObservableList<SupplyOrder> order =
            FXCollections.observableArrayList();
    /**  The data as an observable list for purchases. */
    private static ObservableList<Purchase> purchases =
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
    /** Specify the different choices for the color of an effect. */
    private static final ObservableList<String> EFFECT_COLOR_CHOICE_LIST =
            FXCollections.observableArrayList(EffectColor.Blue.toString(),
                    EffectColor.Green.toString(), EffectColor.Red.toString());
    /** The URI for the space of the fireworks factory. */
    private static URI spaceURI;

    /** Table for the purchase orders. */
    @FXML
    private TableView<Purchase> purchaseTable;
    /** Save data shown in the purchase table. */
    @FXML
    private TableColumn<Purchase, Number> purchaseBuyerIdColumn,
            purchaseIdColumn, purchaseNumberOrderedColumn,
            purchaseNumberProducedColumn;
    /** Save data shown in the purchase table. */
    @FXML
    private TableColumn<Purchase, String> purchaseStatusColumn,
            purchaseStorageAddressColumn, purchaseEffectColors;

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
            supplierCasingIdColumn, packerIdColumn, purchaseIdRocketColumn,
            buyerIdColumn;

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
    /** Saves data shown in the color column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedColorColumn;

    /** Label for the current number of the blue effects in the container.*/
    @FXML
    private Label blueEffectCounterLabel;
    /** Saves data shown in the blueEffectCounterLabel. */
    private static Integer blueEffectCounter = 0;
    /** Saves data shown in the blueEffectCounterLabel. */
    private static IntegerProperty blueEffectCounterProperty =
            new SimpleIntegerProperty(blueEffectCounter);
    /** Label for the current number of the green effects in the container.*/
    @FXML
    private Label greenEffectCounterLabel;
    /** Saves data shown in the greenEffectCounterLabel. */
    private static Integer greenEffectCounter = 0;
    /** Saves data shown in the greenEffectCounterLabel. */
    private static IntegerProperty greenEffectCounterProperty =
            new SimpleIntegerProperty(greenEffectCounter);
    /** Label for the current number of the red effects in the container.*/
    @FXML
    private Label redEffectCounterLabel;
    /** Saves data shown in the redEffectCounterLabel. */
    private static Integer redEffectCounter = 0;
    /** Saves data shown in the redEffectCounterLabel. */
    private static IntegerProperty redEffectCounterProperty =
            new SimpleIntegerProperty(redEffectCounter);

    /** Label for the current number of elements in the casing container. */
    @FXML
    private Label casingsCounterLabel;
    /** Saves data shown in the casingsCounterLabel. */
    private static Integer casingsCounter = 0;
    /** Saves data shown in the casingsCounterLabel. */
    private static IntegerProperty casingsCounterProperty =
            new SimpleIntegerProperty(casingsCounter);
    /** Label for the current number of elements in the wood container. */
    @FXML
    private Label woodCounterLabel;
    /** Saves data shown in the woodCounterLabel. */
    private static Integer woodCounter = 0;
    /** Saves data shown in the woodCounterLabel. */
    private static IntegerProperty woodCounterProperty =
            new SimpleIntegerProperty(woodCounter);
    /** Label for the current number of elements in the propellant container. */
    @FXML
    private Label propellantCounterLabel;
    /** Saves data shown in the propellantCounterLabel. */
    private static Integer propellantCounter = 0;
    /** Saves data shown in the propellantCounterLabel. */
    private static IntegerProperty propellantCounterProperty =
            new SimpleIntegerProperty(propellantCounter);

    /** Label for the current number of open propellant charges in the
     * container. */
    @FXML
    private Label numberOpenPropellantLabel;
    /** Saves data shown in the numberOpenPropellant. */
    private static Integer numberOpenPropellantCounter = 0;
    /** Saves data shown in the numberOpenPropellant. */
    private static IntegerProperty numberOpenPropellantCounterProperty =
            new SimpleIntegerProperty(numberOpenPropellantCounter);
    /** Label for the current quantity in grams of open propellant charges in
     * the container. */
    @FXML
    private Label quantityOpenPropellantLabel;
    /** Saves data shown in the quantityOpenPropellant. */
    private static Integer quantityOpenPropellantCounter = 0;
    /** Saves data shown in the quantityOpenPropellant. */
    private static IntegerProperty quantityOpenPropellantCounterProperty =
            new SimpleIntegerProperty(quantityOpenPropellantCounter);

    /** Displays the number of rockets in the current rocket table. */
    @FXML
    private Label numberRocketsLabel;
    /** The number of produced rockets. */
    @FXML
    private static IntegerProperty numberRocketsProperty =
            new SimpleIntegerProperty(0);
    /** The number of shipped rockets. */
    @FXML
    private static IntegerProperty numberShippedRocketsProperty =
            new SimpleIntegerProperty(0);
    /** The number of trashed rockets. */
    @FXML
    private static IntegerProperty numberTrashedRocketsProperty =
            new SimpleIntegerProperty(0);

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // initialize purchase table
        purchaseBuyerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getBuyerId());
        purchaseIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseId());
        purchaseStatusColumn.setCellValueFactory(
                cellData -> cellData.getValue().getStatusProperty());
        purchaseNumberProducedColumn.setCellValueFactory(
                cellData ->
                        cellData.getValue().getNumberFinishedRocketsProperty());
        purchaseNumberOrderedColumn.setCellValueFactory(
                cellData -> cellData.getValue().getNumberRocketsProperty());
        purchaseEffectColors.setCellValueFactory(
                cellData -> cellData.getValue().getEffectColorsProperty());
        purchaseStorageAddressColumn.setCellValueFactory(
                cellData -> cellData.getValue().getBuyerURI());


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
        buyerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseBuyerIdProperty());
        purchaseIdRocketColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseIdProperty());

        // initialize current warehouse labels
        casingsCounterLabel.textProperty().bind(
                Bindings.convert(casingsCounterProperty));
        blueEffectCounterLabel.textProperty().bind(
                Bindings.convert(blueEffectCounterProperty));
        greenEffectCounterLabel.textProperty().bind(
                Bindings.convert(greenEffectCounterProperty));
        redEffectCounterLabel.textProperty().bind(
                Bindings.convert(redEffectCounterProperty));
        propellantCounterLabel.textProperty().bind(
                Bindings.convert(propellantCounterProperty));
        woodCounterLabel.textProperty().bind(
                Bindings.convert(woodCounterProperty));
        numberOpenPropellantLabel.textProperty().bind(
                Bindings.convert(numberOpenPropellantCounterProperty));
        quantityOpenPropellantLabel.textProperty().bind(
                Bindings.convert(quantityOpenPropellantCounterProperty));
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberRocketsProperty));

        //  initialize supplier table
        supplierNameColumn.setCellValueFactory(
                cellData -> cellData.getValue().supplierNameProperty());
        supplierNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        orderedTypeColumn.setCellValueFactory(
                cellData -> cellData.getValue().typeProperty());
        orderedTypeColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(TYPES_CHOICE_LIST));

        orderedColorColumn.setCellValueFactory(
                cellData -> cellData.getValue().colorProperty());
        orderedColorColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_COLOR_CHOICE_LIST));

        orderedQuantityColumn.setCellValueFactory(
                cellData -> cellData.getValue().quantityProperty());
        orderedQuantityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        orderedQualityColumn.setCellValueFactory(
                cellData -> cellData.getValue().qualityProperty());
        orderedQualityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        //CHECKSTYLE:OFF
        order.add(new SupplyOrder("Hulk", Casing.toString(), EffectColor.Blue,
                50, 100));
        order.add(new SupplyOrder("Iron Man", Wood.toString(), EffectColor.Blue,
                50, 100));
        order.add(new SupplyOrder("Captain America", Effect.toString(),
                EffectColor.Blue, 50, 100));
        order.add(new SupplyOrder("Batman", Effect.toString(), EffectColor.Red,
                50, 60));
        order.add(new SupplyOrder("Thor", Effect.toString(), EffectColor.Green,
                50, 60));
        order.add(new SupplyOrder("Seaman", Propellant.toString(),
                EffectColor.Green, 50, 100));
        order.add(new SupplyOrder("Hawk", Propellant.toString(),
                EffectColor.Red, 50, 100));

        // Add a Purchase to the Container
        //CHECKSTYLE:OFF
        Purchase purchase = new Purchase(1, 10, EffectColor.Red,
                EffectColor.Green, EffectColor.Blue, URI.create("xvsm://localhost:9876"));
        //CHECKSTYLE:ON
        RequestContext context = new RequestContext();
        context.setProperty("newPurchase", 1);

        try {
        ContainerReference containerReference = capi.lookupContainer(
                "purchase",
                spaceURI,
                MzsConstants.RequestTimeout.TRY_ONCE,
                null, null, context);
            capi.write(asList(new Entry(purchase)), containerReference,
                    MzsConstants.RequestTimeout.TRY_ONCE, null, null, context);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        //CHECKSTYLE:ON

        supplyTable.setItems(order);

        rocketTable.setItems(rockets);
        numberRocketsProperty.set(rockets.size());
        purchaseTable.setItems(purchases);
    }

    /**
     * The faulty rocket is displayed in the trash table.
     *
     * @param rocket which is trashed
     */
    public static void addRocketToTrash(final Rocket rocket) {
        Platform.runLater(() -> {
            trashedRocketsList.add(rocket);
            numberTrashedRocketsProperty.set(trashedRocketsList.size());
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
            numberShippedRocketsProperty.set(packedRocketsList.size());
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
                numberRocketsProperty.set(rockets.size());
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
                    numberOpenPropellantCounter);
            quantityOpenPropellantCounterProperty.set(
                    quantityOpenPropellantCounter);
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
            propellantCounterProperty.set(propellantCounter);
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
    public static void reduceCasingWood(final int difference) {
        Platform.runLater(() -> {
            casingsCounter = casingsCounter - difference;
            casingsCounterProperty.set(casingsCounter);
            woodCounter = woodCounter - difference;
            woodCounterProperty.set(woodCounter);
        });
    }


    /**
     * Method to change the counter for the effect labels in the FireWorks GUI.
     *
     * @param effectColor
     *          color of the counter which changes
     * @param difference
     *          value which should be added or subtracted to the effect label
     */
    public static void changeEffectLabels(final EffectColor effectColor,
                                          final int difference) {
        Platform.runLater(() -> {
            switch (effectColor) {
                case Blue:
                    blueEffectCounter += difference;
                    blueEffectCounterProperty.set(blueEffectCounter);
                    break;
                case Red:
                    redEffectCounter += difference;
                    redEffectCounterProperty.set(redEffectCounter);
                    break;
                case Green:
                    greenEffectCounter += difference;
                    greenEffectCounterProperty.set(greenEffectCounter);
                    break;
                default:
                    System.err.println("Effect with wrong color!");
            }
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
                casingsCounterProperty.set(casingsCounter);
            }
            if (containerId.equals(propellantContainer.getId())) {
                propellantCounter = propellantCounter + difference;
                propellantCounterProperty.set(propellantCounter);
            }
            if (containerId.equals(woodContainer.getId())) {
                woodCounter = woodCounter + difference;
                woodCounterProperty.set(woodCounter);
            }
        });
    }
    /**
     * Add a new purchase order to the Table in the GUI.
     *
     * @param purchase
     *          the new purchase order which is to be included in the table
     */
    public static void addPurchaseToTable(final Purchase purchase) {
        Platform.runLater(() -> purchases.add(purchase));
    }

    /**
     * Update the purchase in the GUI table.
     *
     * @param updatedPurchase
     *          Purchase with the updated values to replace the old purchase
     */
    public static void updatePurchaseTable(final Purchase updatedPurchase) {
        Platform.runLater(() -> {
            for (int index = 0; index < purchases.size(); index++) {
                Purchase purchase = purchases.get(index);
                int buyerId = purchase.getBuyerId().intValue();
                int purchaseId = purchase.getPurchaseId().intValue();
                int newBuyerId = updatedPurchase.getBuyerId().intValue();
                int newPurchaseId = updatedPurchase.getPurchaseId().intValue();
                if (buyerId == newBuyerId && purchaseId == newPurchaseId) {
                    purchases.set(index, updatedPurchase);
                    break;
                }
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
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberShippedRocketsProperty));
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
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberTrashedRocketsProperty));
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
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberRocketsProperty));
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
     * This method will be invoked when the color of a material is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setColor(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setColor(
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
        ContainerAspect writePurchasesToContainer = new PurchaseAspects();
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
            capi.addContainerAspect(writePurchasesToContainer,
                    purchaseContainer, iPoints, null);

            // create the container where the ordered rockets are stored
            orderedRocketsContainer = capi.createContainer(
                    "orderedRockets",
                    spaceURI,
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(orderedRocketsContainerAspect,
                    orderedRocketsContainer, iPoints, null);
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
