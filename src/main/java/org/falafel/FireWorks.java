package org.falafel;

/* -- Imports -------------------------------------------------------------- */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
public final class FireWorks extends Application {

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(FireWorks.class);
    /** The space where we want to store our Material. */
    private static MzsCore mozartSpace;
    /** Reference to the API for the space. */
    private static Capi capi;
    /** The container for storing the wood supplies. */
    private static ContainerReference woodContainer;

    /**
     * Create the space and the core API.
     */
    private static void initSpace() {

        mozartSpace = DefaultMzsCore.newInstance();
        capi = new Capi(mozartSpace);

        try {
            woodContainer = capi.createContainer("Wood",
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
            capi.destroyContainer(woodContainer, null);
        } catch (MzsCoreException e) {
            e.printStackTrace();
        }
        mozartSpace.shutdown(true);
        LOGGER.info("Closed space");
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
        Supplier supplier;
        final int numberOfSuppliers = 1;

        for (int supplierId = 1;
             supplierId <= numberOfSuppliers;
             supplierId++) {
            supplier = new Supplier(supplierId,
                    mozartSpace.getConfig().getSpaceUri());
            supplier.start();
        }
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
        final int windowWidth = 200;
        final int windowHeight = 100;

        Parent root = FXMLLoader.load(
                getClass().getResource("/FireWorks.fxml"));

        primaryStage.setTitle("Fireworks Factory");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                closeSpace();
            }
        });
        primaryStage.setScene(new Scene(root, windowWidth, windowHeight));
        primaryStage.show();
    }
}
