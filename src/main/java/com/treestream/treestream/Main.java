package com.treestream.treestream;

import javafx.application.Application;
import javafx.application.Platform;
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

        primaryStage.setTitle("JavaFX Application with Draggable Objects");
        primaryStage.setScene(scene);

        // Set the window size
        setWindowSize(primaryStage);

        // Show the stage
        primaryStage.show();

        // Center the window after it's shown
        centerWindow(primaryStage);
    }

    private void setWindowSize(Stage primaryStage) {
        // Get the primary screen's visual bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Calculate desired width and height (70% of screen size)
        double width = screenBounds.getWidth() * 0.7;
        double height = screenBounds.getHeight() * 0.7;

        // Set the stage size
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
    }

    private void centerWindow(Stage primaryStage) {
        // Center the stage after it's shown
        Platform.runLater(() -> {
            primaryStage.centerOnScreen();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
