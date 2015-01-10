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
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.config.TcpSocketConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.falafel.EffectColor.Blue;
import static org.falafel.EffectColor.Green;
import static org.falafel.EffectColor.Red;
import static org.falafel.Purchase.PurchaseStatus.Shipped;
import static org.mozartspaces.capi3.Selector.COUNT_ALL;
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
    /** The URI of this buyer. */
    private static URI buyerURI;

    /** The URI of the fireworks factory. */
    private static URI fireWorksSpaceURI;

    /** The space of the buyer. */
    private static MzsCore space;
    /** The port for the space of the buyer. */
    private static int spacePort;
    /** Reference to the API for the buyer space. */
    private static Capi capi;
    /** The container for the local purchases of this buyer. */
    private static ContainerReference purchaseContainer;
    /** The container for all rockets already shipped to this buyer. */
    private static ContainerReference rocketContainer;

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

        int maxPurchaseId = 0;
        int purchaseId;
        int purchaseSize;

        Configuration configuration;
        Rocket rocketTemplate;
        /* Save purchases using their id as key */
        Map<Integer, Purchase> oldPurchases = new HashMap<>();
        ArrayList<Purchase> outstandingPurchases = new ArrayList<>();
        ArrayList<Rocket> rockets;
        ArrayList<Rocket> rocketsFromFireWorksFactory = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();

        configuration = new Configuration();
        configuration.getPersistenceConfiguration().setPersistenceProfile(
                PersistenceContext.TRANSACTIONAL_BERKELEY);
        configuration.getPersistenceConfiguration().getPersistenceProperties().
                setProperty("berkeley-location",
                        System.getProperty("java.io.tmpdir") + File.separator
                                + "xvsm" + spacePort);
        ((TcpSocketConfiguration)
                configuration.getTransportConfigurations().get(
                        "xvsm")).setReceiverPort(spacePort);
        buyerURI = URI.create("xvsm://localhost:" + spacePort);
        configuration.setSpaceUri(buyerURI);

        space = DefaultMzsCore.newInstance(configuration);

        capi = new Capi(space);

        /* Read local containers */
        try {
            purchaseContainer = CapiUtil.lookupOrCreateContainer("purchase",
                    space.getConfig().getSpaceUri(), null, null, capi);
            rocketContainer = CapiUtil.lookupOrCreateContainer("rockets",
                    space.getConfig().getSpaceUri(), null, null, capi);

            for (Serializable purchase : capi.read(purchaseContainer,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null)) {
                purchaseId = ((Purchase) purchase).getPurchaseId().intValue();

                oldPurchases.put(purchaseId, (Purchase) purchase);
                if (purchaseId > maxPurchaseId) {
                    maxPurchaseId = purchaseId;
                }
            }
            Purchase.setNextPurchaseId(maxPurchaseId + 1);
            rockets = capi.read(rocketContainer,
                    AnyCoordinator.newSelector(COUNT_ALL), TRY_ONCE, null);
            for (Rocket rocket : rockets) {
                Purchase purchase = oldPurchases.get(
                        rocket.getPurchaseIdProperty().intValue());
                if (purchase != null) {
                    purchase.setStatusToShipped();
                } else {
                    LOGGER.warn("No Purchase for rocket: " + rocket);
                }
            }
            outstandingPurchases.addAll(oldPurchases.values().stream().filter(
                    purchase -> purchase.getStatus() != Shipped).collect(
                    Collectors.toList()));

        } catch (MzsCoreException e) {
            e.printStackTrace();
            System.exit(1);
        }

        /* Read container from fireworks factory */
        try {
            ContainerReference container =
                    capi.lookupContainer("orderedRockets",
                            fireWorksSpaceURI, TRY_ONCE, null);

            /* Try to get the rockets for each outstanding purchase. */
            for (Purchase purchase : outstandingPurchases) {
                purchaseId = purchase.getPurchaseId().intValue();
                purchaseSize = purchase.getNumberRocketsProperty().intValue();

                rocketTemplate = new Rocket(null, null, null, null, null, null,
                        null, new Purchase(buyerId, purchaseId));
                try {
                    rockets = capi.read(container,
                            asList(LindaCoordinator.newSelector(rocketTemplate,
                                    COUNT_ALL)),
                            MzsConstants.RequestTimeout.TRY_ONCE,
                            null, null, null);
                } catch (CountNotMetException e) {
                    continue;
                }
                if (rockets.size() == purchaseSize) {
                    rocketsFromFireWorksFactory.addAll(
                            capi.take(container,
                                    asList(LindaCoordinator.newSelector(
                                            rocketTemplate,
                                            COUNT_ALL)),
                                    MzsConstants.RequestTimeout.TRY_ONCE,
                                    null, null, null));
                    oldPurchases.get(purchaseId).setStatusToFinished();
                }
            }
        } catch (MzsCoreException e) {
            LOGGER.warn("Could not get rockets from the fireworks factory! "
                    + "Most likely the fireworks factory is not online.");
        }

        /* Write rockets from fireworks factory into local container */
        entries.addAll(rocketsFromFireWorksFactory.stream().map(
                Entry::new).collect(Collectors.toList()));
        if (!entries.isEmpty()) {
            try {
                capi.write(entries, rocketContainer);
            } catch (MzsCoreException e) {
                e.printStackTrace();
            }
        }

        /* Update GUI */

        //CHECKSTYLE:OFF
        purchases.addAll(asList(
            new Purchase(buyerId, 1, Red, Green, Blue, buyerURI),
            new Purchase(buyerId, 5, Red, Blue, Blue, buyerURI),
            new Purchase(buyerId, 1, Green, Green, Green, buyerURI))
        );
        //CHECKSTYLE:ON

        purchased.addAll(oldPurchases.values());
        try {
            capi.addContainerAspect(new BuyerRocketsDeliveredAspect(),
                    rocketContainer, new HashSet<>(asList(POST_WRITE)), null);
        } catch (MzsCoreException e) {
            LOGGER.error("Could not add container aspect for rocket container");
        }
    }

    /** Set the status of the given purchase to finished.
     *
     *  @param purchaseId
     *          The id of the purchase for which the status should be set
     */
    public static void setPurchaseStatusToShipped(final int purchaseId) {
        Platform.runLater(() -> {
            int purchaseToUpdateIndex = 0;
            Purchase purchaseToUpdate = null;

            /* We need to set the purchase in the ObservableArrayList.
               If we just change the status of the purchase in the list, then
               the GUI will not show the updated value. */
            for (int purchase = 0; purchase < purchased.size(); purchase++) {
                if (purchased.get(purchase).getPurchaseId().intValue()
                        == purchaseId) {
                    purchaseToUpdateIndex = purchase;
                    purchaseToUpdate = purchased.get(purchase);
                    break;
                }
            }

            if (purchaseToUpdate != null) {
                purchaseToUpdate.setStatusToShipped();
                purchased.set(purchaseToUpdateIndex, purchaseToUpdate);
            }
        });
    }

    /** Close resources handled by this buyer. */
    private void closeBuyer() {
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
        purchases.add(new Purchase(buyerId, buyerURI));
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
                    capi.lookupContainer("purchase", fireWorksSpaceURI,
                            TRY_ONCE, null);

            for (Purchase purchase : purchases) {
                capi.write(asList(new Entry(purchase)),
                        container, TRY_ONCE, null, null, context);
                capi.write(new Entry(purchase), purchaseContainer);
            }
            purchased.addAll(purchases);
            purchases.clear();

        } catch (MzsCoreException e) {
            LOGGER.warn("Could not order rockets! Most likely the fireworks "
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
            capi.delete(purchaseContainer);
            capi.delete(rocketContainer);
        } catch (CountNotMetException c) {
            LOGGER.warn("Could not delete all elements or deleted empty "
                    + "container");
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
