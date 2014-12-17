package org.falafel;

import javafx.application.Application;
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

    @FXML
    private TableView newPurchaseTableView;
    @FXML
    private TableColumn<SupplyOrder, String> newQuantityPurchaseColumn,
            newColor1PurchaseColumn, newColor2PurchaseColumn,
            newColor3PurchaseColumn;
    @FXML
    private TableView purchaseTableView;
    @FXML
    private TableColumn<SupplyOrder, String> statusPurchaseColumn,
            color1PurchaseColumn, color2PurchaseColumn,
            color3PurchaseColumn;
    @FXML
    private TableColumn<SupplyOrder, Number> idPurchaseColumn,
            quantityPurchaseColumn;


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

    private void closeBuyer() {
    }

    public void newPurchase(ActionEvent actionEvent) {
    }

    public void clearPurchase(ActionEvent actionEvent) {
    }

    public void orderPurchase(ActionEvent actionEvent) {
    }
}
