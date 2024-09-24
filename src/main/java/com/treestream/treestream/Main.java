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

        // Apply custom styles
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setTitle("JavaFX Application with Draggable Objects");
        primaryStage.setScene(scene);

        // Set the window size
        setWindowSize(primaryStage);

        // Show the stage
        primaryStage.show();
        centerWindow(primaryStage);

        // Get the controller
        MainController controller = loader.getController();

        // Set the mainPanel size to match the window size
        double windowWidth = primaryStage.getWidth();
        double windowHeight = primaryStage.getHeight();

        // Adjust for menu bar height
        double menuBarHeight = 40; // Approximate height of the toolbar
        controller.setMainPanelSize(windowWidth, windowHeight - menuBarHeight);
    }

    private void setWindowSize(Stage primaryStage) {
        // Get the primary screen's bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Calculate 70% of screen width and height
        double width = screenBounds.getWidth() * 0.7;
        double height = screenBounds.getHeight() * 0.7;

        // Set the stage size
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
    }

    private void centerWindow(Stage primaryStage) {
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
