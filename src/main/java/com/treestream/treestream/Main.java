package com.treestream.treestream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main FXML layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Scene scene = new Scene(loader.load());

        // Apply BootstrapFX stylesheet
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        // Optional: Add custom styles
        // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Get the primary screen's bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Calculate 70% of screen width and height
        double width = screenBounds.getWidth() * 0.7;
        double height = screenBounds.getHeight() * 0.7;

        // Set the window size
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);

        // Center the window
        primaryStage.centerOnScreen();

        primaryStage.setTitle("JavaFX Application with Draggable Objects");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
