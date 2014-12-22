package org.falafel;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

import java.io.IOException;
import java.net.URI;

import static org.falafel.EffectColor.Blue;
import static org.falafel.EffectColor.Green;
import static org.falafel.EffectColor.Red;

/**
 * This class represents a buyer of rockets.
 *
 * A buyer orders rockets from the factory and stores them into his space after
 * the rockets were produced.
 */
public final class Buyer extends Application {

    /** The unique identification of this buyer. */
    private static Integer buyerId;
    /** The URI of the fireworks factory. */
    private static URI fireWorksSpaceURI;

    /** The space of the buyer. */
    private static MzsCore space;
    /** The port for the space of the buyer. */
    private static int spacePort;
    /** Reference to the API for the buyer space. */
    private static Capi capiSpace;
    /** The container for the local purchases of this buyer. */
    private static ContainerReference purchaseContainer;

    /** The data stored in the table for new purchases. */
    private static ObservableList<Purchase> purchases =
            FXCollections.observableArrayList();
    /** The data stored in the table for purchased items. */
    private static ObservableList<Purchase> purchased =
            FXCollections.observableArrayList();

    /** Specify the different effect colors a buyer can purchase. */
    private static final ObservableList<String> EFFECT_CHOICES =
            FXCollections.observableArrayList(Red.toString(),
                    Blue.toString(), Green.toString());

    /** The tables for new and existing purchases. */
    @FXML
    private TableView<Purchase> purchaseTableView, newPurchaseTableView;

    /** The columns for purchase properties that are numbers. */
    @FXML
    private TableColumn<Purchase, Number>
            idPurchaseColumn,
            quantityPurchaseColumn;

    /** The columns for purchase properties that can not be represented by
     *  simple numbers. */
    @FXML
    private TableColumn<Purchase, String>
            statusPurchaseColumn,
            color1PurchaseColumn,
            color2PurchaseColumn,
            color3PurchaseColumn,
            newQuantityPurchaseColumn,
            newColor1PurchaseColumn,
            newColor2PurchaseColumn,
            newColor3PurchaseColumn;

    /**
     * Start the buyer.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {

        final int numberArguments = 3;

        if (arguments.length != numberArguments) {
            System.err.println("Usage: buyer <Id> <Space URI> <Port>");
            return;
        }
        try {
            buyerId = Integer.parseInt(arguments[0]);
            fireWorksSpaceURI = URI.create(arguments[1]);
            spacePort = Integer.parseInt(arguments[2]);
        } catch (Exception e) {
            System.err.println("Please supply valid command line arguments!");
            System.exit(1);
        }

        initSpace();
        launch(arguments);
    }

    /**
     * Initialize the data for the graphical user interface.
     */
    @FXML
    private void initialize() {
        URI buyerSpaceURI = space.getConfig().getSpaceUri();

        newQuantityPurchaseColumn.setCellValueFactory(
                cellData -> Bindings.convert(
                        cellData.getValue().getNumberRocketsProperty()));
        newQuantityPurchaseColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        newColor1PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getFirstColorProperty());
        newColor1PurchaseColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_CHOICES));

        newColor2PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSecondColorProperty());
        newColor2PurchaseColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_CHOICES));

        newColor3PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getThirdColorProperty());
        newColor3PurchaseColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_CHOICES));

        statusPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getStatusProperty());
        color1PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getFirstColorProperty());
        color2PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSecondColorProperty());
        color3PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getThirdColorProperty());
        idPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseId());
        quantityPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getNumberRocketsProperty());

        //CHECKSTYLE:OFF
        purchases.add(
                new Purchase(buyerId, 10, Red, Green, Blue, buyerSpaceURI));
        //CHECKSTYLE:ON

        newPurchaseTableView.setItems(purchases);
        purchaseTableView.setItems(purchased);

    }

    @Override
    public void start(final Stage primaryStage) throws IOException {

        final float minWidth = 300;
        final float minHeight = 200;

        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);

        Parent root = FXMLLoader.load(getClass().getResource("/Buyer.fxml"));
        primaryStage.setTitle("Buyer " + buyerId + "— "
                + space.getConfig().getSpaceUri() + "— " + fireWorksSpaceURI);
        primaryStage.setOnCloseRequest(event -> closeBuyer());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Initialize the space.
     */
    private static void initSpace() {
        space = DefaultMzsCore.newInstance(spacePort);
        capiSpace = new Capi(space);

        try {
            purchaseContainer = capiSpace.createContainer(
                    "Purchases",
                    space.getConfig().getSpaceUri(),
                    MzsConstants.Container.UNBOUNDED,
                    null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
    }

    /** Close resources handled by this buyer. */
    private void closeBuyer() {
        try {
            capiSpace.destroyContainer(purchaseContainer, null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        space.shutdown(true);
    }

    /**
     * Create a new purchase in the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void newPurchase(final ActionEvent actionEvent) {
        purchases.add(new Purchase(buyerId));
    }

    /**
     * Remove all purchases from the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void clearPurchase(final ActionEvent actionEvent) {
        purchases.clear();
    }

    /**
     * Order all purchases currently stored in the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void orderPurchase(final ActionEvent actionEvent) {
        purchased.addAll(purchases);
        purchases.clear();
    }

    /**
     * This method will be invoked when the quantity for a purchase is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setQuantity(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();
        stCellEditEvent.getTableView().getItems().get(row).setNumberRockets(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when the first effect color for a purchase
     * is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setFirstEffectColor(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();

        stCellEditEvent.getTableView().getItems().get(row).setFirstEffectColor(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when the second effect color for a purchase
     * is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setSecondEffectColor(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();

        stCellEditEvent.getTableView().getItems().get(row).setSecondEffectColor(
                stCellEditEvent.getNewValue());

    }

    /**
     * This method will be invoked when the third effect color for a purchase
     * is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setThirdEffectColor(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();

        stCellEditEvent.getTableView().getItems().get(row).setThirdEffectColor(
                stCellEditEvent.getNewValue());
    }
}
