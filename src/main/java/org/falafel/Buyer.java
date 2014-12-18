package org.falafel;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

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
    private static URI spaceUri;

    /** The data stored in the table for new purchases. */
    private static ObservableList<Purchase> purchases =
            FXCollections.observableArrayList();

    /** The tables for new and existing purchases. */
    @FXML
    private TableView<Purchase> purchaseTableView, newPurchaseTableView;

    /** The columns for purchase properties that are numbers. */
    @FXML
    private TableColumn<Purchase, Number>
            newQuantityPurchaseColumn,
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
            newColor1PurchaseColumn,
            newColor2PurchaseColumn,
            newColor3PurchaseColumn;

    /**
     * Start the buyer.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {

        if (arguments.length != 2) {
            System.err.println("Usage: buyer <Id> <Space URI>");
            return;
        }
        try {
            buyerId = Integer.parseInt(arguments[0]);
            spaceUri = URI.create(arguments[1]);
        } catch (Exception e) {
            System.err.println("Please supply valid command line arguments!");
            System.exit(1);
        }

        launch(arguments);
    }

    /**
     * Initialize the data for the graphical user interface.
     */
    @FXML
    private void initialize() {
        newQuantityPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getNumberRocketsProperty());
        newColor1PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getFirstColorProperty());
        newColor2PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSecondColorProperty());
        newColor3PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getThirdColorProperty());
        //CHECKSTYLE:OFF
        purchases.add(
                new Purchase(1, 2, 10, Red, Green, Blue, URI.create("bla")));
        //CHECKSTYLE:ON

        newPurchaseTableView.setItems(purchases);

    }

    @Override
    public void start(final Stage primaryStage) throws IOException {

        final float minWidth = 300;
        final float minHeight = 200;

        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);

        Parent root = FXMLLoader.load(getClass().getResource("/Buyer.fxml"));
        primaryStage.setTitle("Buyer " + buyerId + "— " + spaceUri);
        primaryStage.setOnCloseRequest(event -> closeBuyer());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /** Close resources handled by this buyer. */
    private void closeBuyer() {
    }

    /**
     * Create a new purchase in the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void newPurchase(final ActionEvent actionEvent) {
    }

    /**
     * Remove all purchases from the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void clearPurchase(final ActionEvent actionEvent) {
    }

    /**
     * Order all purchases currently stored in the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void orderPurchase(final ActionEvent actionEvent) {
    }
}
