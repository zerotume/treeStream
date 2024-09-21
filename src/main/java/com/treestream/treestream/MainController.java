package com.treestream.treestream;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class MainController {

    @FXML
    private Pane mainPanel;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ToggleButton zoomInButton;

    @FXML
    private ToggleButton zoomOutButton;

    @FXML
    private ToggleButton moveButton;

    private Scale scaleTransform;
    private Translate translateTransform;

    private double scaleValue = 1.0;
    private final double scaleIncrement = 0.1;
    private final double maxScale = 4.0;
    private final double minScale = 0.25;

    private double mouseX;
    private double mouseY;

    @FXML
    private void initialize() {
        // Initialize transforms
        scaleTransform = new Scale(scaleValue, scaleValue, 0, 0);
        translateTransform = new Translate();

        mainPanel.getTransforms().addAll(scaleTransform, translateTransform);

        // Add event handlers
        mainPanel.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        mainPanel.setOnMouseDragged(event -> {
            if (moveButton.isSelected() || (event.isControlDown() && event.getButton() == MouseButton.PRIMARY)) {
                double deltaX = event.getSceneX() - mouseX;
                double deltaY = event.getSceneY() - mouseY;

                translateTransform.setX(translateTransform.getX() + deltaX);
                translateTransform.setY(translateTransform.getY() + deltaY);

                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
            }
        });

        mainPanel.setOnScroll(this::handleScroll);

        // Deactivate toggle buttons when ESC is pressed
        // Add listener to scene property
        mainPanel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case ESCAPE:
                            zoomInButton.setSelected(false);
                            zoomOutButton.setSelected(false);
                            moveButton.setSelected(false);
                            break;
                        default:
                            break;
                    }
                });
            }
        });
    }

    @FXML
    private void handleAddObject() {
        // Create a new DraggableNodeController instance
        DraggableNodeController node = new DraggableNodeController();
        node.setLayoutX(50);
        node.setLayoutY(50);
        mainPanel.getChildren().add(node);
        node.editName(); // Focus on the TextField for editing

        // Expand canvas if necessary
        expandCanvasIfNeeded(node);
    }

    @FXML
    private void handleZoomInButton() {
        // Toggle zoom in mode
        if (zoomInButton.isSelected()) {
            zoomOutButton.setSelected(false);
            moveButton.setSelected(false);
        }
    }

    @FXML
    private void handleZoomOutButton() {
        // Toggle zoom out mode
        if (zoomOutButton.isSelected()) {
            zoomInButton.setSelected(false);
            moveButton.setSelected(false);
        }
    }

    @FXML
    private void handleMoveButton() {
        // Toggle move mode
        if (moveButton.isSelected()) {
            zoomInButton.setSelected(false);
            zoomOutButton.setSelected(false);
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            zoom(event.getX(), event.getY(), 1 + scaleIncrement);
        } else {
            zoom(event.getX(), event.getY(), 1 - scaleIncrement);
        }
    }

    private void zoom(double x, double y, double factor) {
        double oldScale = scaleValue;
        scaleValue *= factor;

        // Clamp scale value
        scaleValue = Math.max(minScale, Math.min(scaleValue, maxScale));

        double f = scaleValue / oldScale;

        // Adjust the translation to keep the focus on the zoom point
        double dx = (x - (translateTransform.getX() + mainPanel.getLayoutX())) * (f - 1);
        double dy = (y - (translateTransform.getY() + mainPanel.getLayoutY())) * (f - 1);

        scaleTransform.setX(scaleValue);
        scaleTransform.setY(scaleValue);

        translateTransform.setX(translateTransform.getX() - dx);
        translateTransform.setY(translateTransform.getY() - dy);
    }

    // Expand canvas size if nodes are placed beyond current bounds
    private void expandCanvasIfNeeded(DraggableNodeController node) {
        double nodeRight = node.getLayoutX() + node.getBoundsInParent().getWidth();
        double nodeBottom = node.getLayoutY() + node.getBoundsInParent().getHeight();

        double canvasWidth = mainPanel.getPrefWidth();
        double canvasHeight = mainPanel.getPrefHeight();

        boolean expanded = false;

        if (nodeRight > canvasWidth) {
            mainPanel.setPrefWidth(canvasWidth * 2);
            expanded = true;
        }

        if (nodeBottom > canvasHeight) {
            mainPanel.setPrefHeight(canvasHeight * 2);
            expanded = true;
        }

        // Optionally, adjust the position of other nodes or the view
        if (expanded) {
            // Implement any additional logic if needed
        }
    }
}
