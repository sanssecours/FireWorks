package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static org.mozartspaces.core.MzsConstants.Container;
import static org.slf4j.LoggerFactory.getLogger;

/* -- Class ---------------------------------------------------------------- */

/**
 * Main class for the project. This class provides an interface to start
 * suppliers and keep an eye on the progress of the production in the firework
 * factory.
 */
public class FireWorks extends Application {
   /** Different types of Material provided by Suppliers. */
    public static enum MaterialType { Casing, Effect, Propellant, Wood }

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
    /** The running id for the suppliers. */
    private static int supplierId = 1;
    /** The running id for the materials. */
    private static int materialId = 1;

    /**  The data as an observable list for SupplyOrder. */
    private ObservableList<SupplyOrder> order =
            FXCollections.observableArrayList();

    /** Specify the different choices a supplier can provide. */
    private static final ObservableList<String> TYPES_CHOICE_LIST =
            FXCollections.observableArrayList(MaterialType.Casing.toString(),
                    MaterialType.Effect.toString(),
                    MaterialType.Propellant.toString(),
                    MaterialType.Wood.toString());

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
    private static StringProperty effectCounterProperty = new SimpleStringProperty(effectCounter.toString());
    /** Label for the current number of elements in the casing container. */
    @FXML
    private Label casingsCounterLabel;
    /** Saves data shown in the casingsCounterLabel. */
    private static Integer casingsCounter = 0;
    /** Saves data shown in the casingsCounterLabel. */
    private static StringProperty casingsCounterProperty = new SimpleStringProperty(casingsCounter.toString());
    /** Label for the current number of elements in the wood container. */
    @FXML
    private Label woodCounterLabel;
    /** Saves data shown in the woodCounterLabel. */
    private static Integer woodCounter = 0;
    /** Saves data shown in the woodCounterLabel. */
    private static StringProperty woodCounterProperty = new SimpleStringProperty(woodCounter.toString());
    /** Label for the current number of elements in the propellant container. */
    @FXML
    private Label propellantCounterLabel;
    /** Saves data shown in the propellantCounterLabel. */
    private static Integer propellantCounter = 0;
    /** Saves data shown in the propellantCounterLabel. */
    private static StringProperty propellantCounterProperty = new SimpleStringProperty(propellantCounter.toString());

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        casingsCounterLabel.textProperty().bind(casingsCounterProperty);
        effectCounterLabel.textProperty().bind(effectCounterProperty);
        propellantCounterLabel.textProperty().bind(propellantCounterProperty);
        woodCounterLabel.textProperty().bind(woodCounterProperty);

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

        order.add(new SupplyOrder());
        supplyTable.isEditable();
        supplyTable.setItems(order);
    }
    /**
     * Updates the counters in the GUI
     *
     * @param containerId
     *          ID of the changed container
     * @param difference
     *          change of the elements in the container, can be positive or negative
     *
     */
    public static void changeCounterLabels (String containerId, int difference){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (containerId.equals(casingContainer.getId())){
                    casingsCounter = casingsCounter + difference;
                    casingsCounterProperty.set(casingsCounter.toString());
                }
                if (containerId.equals(effectContainer.getId())){
                    effectCounter = effectCounter + difference;
                    effectCounterProperty.set(effectCounter.toString());
                }
                if (containerId.equals(propellantContainer.getId())){
                    propellantCounter = propellantCounter + difference;
                    propellantCounterProperty.set(propellantCounter.toString());
                }
                if (containerId.equals(woodContainer.getId())){
                    woodCounter = woodCounter + difference;
                    woodCounterProperty.set(woodCounter.toString());
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
                    mozartSpace.getConfig().getSpaceUri(), nextOrder, materialId);
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
    public final void newOrder(final ActionEvent actionEvent) {
        order.add(new SupplyOrder());
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
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setQuantity(
                Integer.parseInt(stCellEditEvent.getNewValue()));
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
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setQuality(
                Integer.parseInt(stCellEditEvent.getNewValue()));
    }

    /**
     * Create the space and the core API.
     */
    private static void initSpace() {

        mozartSpace = DefaultMzsCore.newInstance();
        capi = new Capi(mozartSpace);

        ContainerAspect materailContainerAspect = new MaterialAspects();
        Set<ContainerIPoint> ipoints = new HashSet<ContainerIPoint>();
        ipoints.add(ContainerIPoint.POST_WRITE);
        ipoints.add(ContainerIPoint.POST_TAKE);
        try {
            casingContainer = capi.createContainer(
                    MaterialType.Casing.toString(),
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(materailContainerAspect, casingContainer, ipoints, null);
            effectContainer = capi.createContainer(
                    MaterialType.Effect.toString(),
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(materailContainerAspect, effectContainer, ipoints, null);
            propellantContainer = capi.createContainer(
                    MaterialType.Propellant.toString(),
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(materailContainerAspect, propellantContainer, ipoints, null);
            woodContainer = capi.createContainer(
                    MaterialType.Wood.toString(),
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            capi.addContainerAspect(materailContainerAspect, woodContainer, ipoints, null);
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
}
