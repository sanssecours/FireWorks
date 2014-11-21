package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.slf4j.Logger;

import static org.mozartspaces.core.MzsConstants.Container;
import static org.slf4j.LoggerFactory.getLogger;

/* -- Class ---------------------------------------------------------------- */

/**
 * Main class for the project. This class provides an interface to start
 * suppliers and keep an eye on the progress of the production in the firework
 * factory.
 */
public class FireWorks extends Application {

    public static final String CASING = "Casing";
    public static final String EFFECT = "Effect";
    public static final String PROPELLANT = "Propellant";
    public static final String WOOD = "Wood";
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

    /**  The data as an observable list for SupplyOrder */
    private ObservableList<SupplyOrder> Order = FXCollections.observableArrayList();

    private static ObservableList<String> typesChoiceList = FXCollections.observableArrayList (
            new String(CASING),
            new String(EFFECT),
            new String(PROPELLANT),
            new String(WOOD)
    );

    @FXML
    private TableView<SupplyOrder> supplyTable;
    @FXML
    private TableColumn<SupplyOrder, String> supplierNameColumn;
    @FXML
    private TableColumn<SupplyOrder, String> orderedTypeColumn;
    @FXML
    private TableColumn<SupplyOrder, String> orderedQuantityColumn;
    @FXML
    private TableColumn<SupplyOrder, String> orderedQualityColumn;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        Order.add(new SupplyOrder());

        supplyTable.isEditable();

        // Initialize the person table with the two columns.
        supplierNameColumn.setCellValueFactory(cellData -> cellData.getValue().supplierNameProperty());
        supplierNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        supplierNameColumn.isEditable();

        orderedTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        orderedTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(typesChoiceList));
        orderedTypeColumn.isEditable();

        orderedQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        orderedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        orderedQuantityColumn.isEditable();

        orderedQualityColumn.setCellValueFactory(cellData -> cellData.getValue().qualityProperty());
        orderedQualityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        orderedQualityColumn.isEditable();

        supplyTable.setItems(Order);
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

        while(!Order.isEmpty()) {
            nextOrder = Order.remove(0);
            LOGGER.debug(nextOrder.toString());
            supplier = new Supplier(supplierId,
                    mozartSpace.getConfig().getSpaceUri(), nextOrder);
            supplier.start();
            supplierId ++;
        }

        System.out.println("No new order!");


        /*for (int supplierId = 1;
             supplierId <= numberOfSuppliers;
             supplierId++) {
            supplier = new Supplier(supplierId,
                    mozartSpace.getConfig().getSpaceUri());
            supplier.start();
        }*/
    }

    public void setSupplierName(TableColumn.CellEditEvent<SupplyOrder, String> stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(stCellEditEvent.getTablePosition().getRow()).setSupplierName(
                stCellEditEvent.getNewValue());
    }

    public void NewOrder(ActionEvent actionEvent) {
        Order.add(new SupplyOrder());
    }

    public void setType(TableColumn.CellEditEvent<SupplyOrder, String> stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(stCellEditEvent.getTablePosition().getRow()).setType(
                stCellEditEvent.getNewValue());
    }

    public void setQuantity(TableColumn.CellEditEvent<SupplyOrder, String> stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(stCellEditEvent.getTablePosition().getRow()).setQuantity(
                Integer.parseInt(stCellEditEvent.getNewValue()));
    }

    public void setQuality(TableColumn.CellEditEvent<SupplyOrder, String> stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(stCellEditEvent.getTablePosition().getRow()).setQuality(
                Integer.parseInt(stCellEditEvent.getNewValue()));
    }


    /**
     * Create the space and the core API.
     */
    private static void initSpace() {

        mozartSpace = DefaultMzsCore.newInstance();
        capi = new Capi(mozartSpace);

        try {
            casingContainer = capi.createContainer(CASING,
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            effectContainer = capi.createContainer(EFFECT,
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            propellantContainer = capi.createContainer(PROPELLANT,
                    mozartSpace.getConfig().getSpaceUri(),
                    Container.UNBOUNDED,
                    null);
            woodContainer = capi.createContainer(WOOD,
                    mozartSpace.getConfig().getSpaceUri(),
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
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        mozartSpace.shutdown(true);
        LOGGER.info("Closed space");
    }



     /**
     * Start the firework factory.
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
    public void start(final Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/FireWorks.fxml"));

        primaryStage.setTitle("Fireworks Factory");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                closeSpace();
            }
        });
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
