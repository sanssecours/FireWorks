package org.falafel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by Johannes on 21.11.2014.
 */
public class GUIController {
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);
    /**  The data as an observable list for SupplyOrder */
    private ObservableList<SupplyOrder> Order = FXCollections.observableArrayList();

    private ObservableList<String> typesChoiceList = FXCollections.observableArrayList (
            new String("Casing"),
            new String("Effect"),
            new String("Propellant"),
            new String("Wood")
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
        final int numberOfSuppliers = 1;
        if(!Order.isEmpty()) {
            nextOrder = Order.remove(0);
            LOGGER.debug(nextOrder.toString());
        } else
            System.out.println("No new order!");


//        for (int supplierId = 1;
//             supplierId <= numberOfSuppliers;
//             supplierId++) {
//            supplier = new Supplier(supplierId,
//                    mozartSpace.getConfig().getSpaceUri());
//            supplier.start();
//        }
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
}
