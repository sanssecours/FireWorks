package org.falafel;

import javafx.application.Application;
import javafx.application.Platform;
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
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.config.TcpSocketConfiguration;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.falafel.EffectColor.Blue;
import static org.falafel.EffectColor.Green;
import static org.falafel.EffectColor.Red;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
import static org.mozartspaces.core.MzsConstants.Container.UNBOUNDED;
import static org.mozartspaces.core.MzsConstants.RequestTimeout.TRY_ONCE;
import static org.mozartspaces.core.aspects.ContainerIPoint.POST_WRITE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class represents a buyer of rockets.
 *
 * A buyer orders rockets from the factory and stores them into his space after
 * the rockets were produced.
 */
public final class Buyer extends Application {

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Buyer.class);

    /** The unique identification of this buyer. */
    private static Integer buyerId;

    /** The URI of the fireworks factory. */
    private static URI fireWorksSpaceURI;
    /** The space of the fireworks factory. */
    private static MzsCore fireWorksSpace;
    /** Reference to the API for fireworks factory. */
    private static Capi fireWorksCapi;

    /** The space of the buyer. */
    private static MzsCore space;
    /** The port for the space of the buyer. */
    private static int spacePort;
    /** Reference to the API for the buyer space. */
    private static Capi spaceCapi;
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

        final float minWidth = 620;
        final float minHeight = 112;

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

        Configuration configuration;
        ContainerReference rocketContainer;
        /* Save purchases using their id as key */
        Map<Integer, Purchase> oldPurchases = new HashMap<>();
        ArrayList<Rocket> rockets;

        configuration = new Configuration();
        configuration.getPersistenceConfiguration().setPersistenceProfile(
                PersistenceContext.TRANSACTIONAL_BERKELEY);
        ((TcpSocketConfiguration)
                configuration.getTransportConfigurations().get(
                        "xvsm")).setReceiverPort(spacePort);

        fireWorksSpace = DefaultMzsCore.newInstanceWithoutSpace();
        space = DefaultMzsCore.newInstance(configuration);

        spaceCapi = new Capi(space);
        fireWorksCapi = new Capi(fireWorksSpace);

        try {
            purchaseContainer = CapiUtil.lookupOrCreateContainer("purchase",
                    space.getConfig().getSpaceUri(), null, null, spaceCapi);
            rocketContainer = CapiUtil.lookupOrCreateContainer("rockets",
                    space.getConfig().getSpaceUri(), null, null, spaceCapi);
            spaceCapi.addContainerAspect(new BuyerRocketsDeliveredAspect(),
                    rocketContainer, new HashSet<>(asList(POST_WRITE)), null);


            /* Move old purchases into GUI */
            for (Serializable purchase : spaceCapi.read(purchaseContainer,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null)) {
                oldPurchases.put(
                        ((Purchase) purchase).getBuyerId().intValue(),
                        (Purchase) purchase);

            }
            rockets = spaceCapi.read(rocketContainer,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            for (Rocket rocket: rockets) {
                oldPurchases.get(rocket.getPurchaseIdProperty().intValue()).
                        setStatusToFinished();
            }
            purchased.addAll(oldPurchases.values());

        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
    }

    /** Set the status of the given purchase to finished.
     *
     *  @param purchaseId
     *          The id of the purchase for which the status should be set
     */
    public static void setPurchaseStatusToFinished(final int purchaseId) {
        Platform.runLater(() -> {
            for (Purchase purchase : purchased) {
                if (purchase.getPurchaseId().intValue() == purchaseId) {
                    purchase.setStatusToFinished();
                    break;
                }
            }
        });
    }

    /** Close resources handled by this buyer. */
    private void closeBuyer() {
        space.shutdown(true);
        fireWorksSpace.shutdown(true);
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
        try {
            RequestContext context = new RequestContext();
            context.setProperty("newPurchase", 1);

            ContainerReference container =
                    fireWorksCapi.lookupContainer("purchase", fireWorksSpaceURI,
                            TRY_ONCE, null);

            for (Purchase purchase : purchases) {
                fireWorksCapi.write(asList(new Entry(purchase)),
                        container, TRY_ONCE, null, null, context);
                spaceCapi.write(new Entry(purchase), purchaseContainer);
            }
            purchased.addAll(purchases);
            purchases.clear();

        } catch (MzsCoreException e) {
            LOGGER.error("Could not order rockets! Most likely the fireworks "
                    + "factory is not online.");
        }
    }

    /**
     * Remove all current purchases from the space of the buyer.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void removePurchases(final ActionEvent actionEvent) {
        try {
            spaceCapi.destroyContainer(purchaseContainer, null);
            purchaseContainer = spaceCapi.createContainer("purchase",
                    space.getConfig().getSpaceUri(), UNBOUNDED, null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        purchased.clear();
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
