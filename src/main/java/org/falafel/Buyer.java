package org.falafel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        Parent root = FXMLLoader.load(getClass().getResource("/Buyer.fxml"));
        primaryStage.setTitle("Buyer " + buyerId + "— " + spaceUri);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
