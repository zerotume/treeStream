package com.treestream.treestream;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @FXML
    private ToggleButton flowConnectButton;


    private Scale scaleTransform;
    private Translate translateTransform;

    private double scaleValue = 1.0;
    private final double scaleIncrement = 0.1;
    private final double maxScale = 4.0;
    private final double minScale = 0.25;

    private double mouseX;
    private double mouseY;

    private DraggableNodeController selectedNode = null;
    private boolean flowConnectMode = false;

    private Map<DraggableNodeController, List<DraggableNodeController>> connections = new HashMap<>();
    private List<Arrow> arrows = new ArrayList<>();


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

        // Add listener to scene property
        mainPanel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case ESCAPE:
                            deactivateToggleButtons();
                            break;
                        default:
                            break;
                    }
                });
            }
        });

        // Zoom in/out on mainPanel click when buttons are active
        mainPanel.setOnMouseClicked(event -> {
            if (zoomInButton.isSelected()) {
                zoom(event.getX(), event.getY(), 1 + scaleIncrement);
            } else if (zoomOutButton.isSelected()) {
                zoom(event.getX(), event.getY(), 1 - scaleIncrement);
            }
        });
    }

    public void setMainPanelSize(double width, double height) {
        mainPanel.setPrefWidth(width);
        mainPanel.setPrefHeight(height);

        // Center the scrollPane's view after layout
        Platform.runLater(() -> {
            scrollPane.setHvalue(scrollPane.getHmax() / 2);
            scrollPane.setVvalue(scrollPane.getVmax() / 2);
        });
    }

    @FXML
    private void handleAddObject() {
        // Create a new DraggableNodeController instance
        DraggableNodeController node = new DraggableNodeController(this);

        // Place the node at the center of the view
        double centerX = mainPanel.getPrefWidth() / 2;
        double centerY = mainPanel.getPrefHeight() / 2;

        node.setLayoutX(centerX - node.getBoundsInLocal().getWidth() / 2);
        node.setLayoutY(centerY - node.getBoundsInLocal().getHeight() / 2);

        mainPanel.getChildren().add(node);
        node.editName(); // Focus on the TextField for editing

        // Expand canvas if necessary
        expandCanvasIfNeeded(node);
    }

    @FXML
    private void handleZoomInButton() {
        if (zoomInButton.isSelected()) {
            zoomOutButton.setSelected(false);
            moveButton.setSelected(false);
        }
    }

    @FXML
    private void handleZoomOutButton() {
        if (zoomOutButton.isSelected()) {
            zoomInButton.setSelected(false);
            moveButton.setSelected(false);
        }
    }

    @FXML
    private void handleMoveButton() {
        if (moveButton.isSelected()) {
            zoomInButton.setSelected(false);
            zoomOutButton.setSelected(false);
            mainPanel.setCursor(javafx.scene.Cursor.OPEN_HAND);
        } else {
            mainPanel.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    @FXML
    private void handleFlowConnectButton() {
        if (flowConnectButton.isSelected()) {
            flowConnectMode = true;
            // Deactivate other modes if necessary
            // For example, deactivate move or zoom modes
        } else {
            flowConnectMode = false;
        }
    }

    public void handleNodeClickedForConnection(DraggableNodeController targetNode) {
        if (selectedNode == null) {
            // Should not happen, but check anyway
            flowConnectButton.setSelected(false);
            flowConnectMode = false;
            return;
        }

        if (selectedNode == targetNode) {
            // Self-connection detected
            showError("Connect failed: self-pointing detected");
            clearSelectedNode();
            flowConnectButton.setSelected(false);
            flowConnectMode = false;
            return;
        }

        // Check for existing connection to prevent cycles
        List<DraggableNodeController> connectedNodes = connections.getOrDefault(targetNode, new ArrayList<>());
        if (connectedNodes.contains(selectedNode)) {
            showError("Connect failed: self-pointing detected");
            clearSelectedNode();
            flowConnectButton.setSelected(false);
            flowConnectMode = false;
            return;
        }

        // Create the connection
        createConnection(selectedNode, targetNode);

        // Clear selection and flow connect mode
        clearSelectedNode();
        flowConnectButton.setSelected(false);
        flowConnectMode = false;
    }

    private void createConnection(DraggableNodeController sourceNode, DraggableNodeController targetNode) {
        // Create an arrow between sourceNode and targetNode
        Arrow arrow = new Arrow(sourceNode, targetNode);

        // Add the arrow to the mainPanel
        mainPanel.getChildren().add(0, arrow); // Add behind nodes

        // Store the connection
        connections.computeIfAbsent(sourceNode, k -> new ArrayList<>()).add(targetNode);
        arrows.add(arrow);
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
        double dx = (x - translateTransform.getX()) * (f - 1);
        double dy = (y - translateTransform.getY()) * (f - 1);

        scaleTransform.setX(scaleValue);
        scaleTransform.setY(scaleValue);

        translateTransform.setX(translateTransform.getX() - dx);
        translateTransform.setY(translateTransform.getY() - dy);
    }



    private void shiftNodesHorizontally(double shift) {
        for (Node child : mainPanel.getChildren()) {
            if (child instanceof DraggableNodeController) {
                DraggableNodeController node = (DraggableNodeController) child;
                node.setLayoutX(node.getLayoutX() + shift);
            }
        }

        // Adjust the translate transform to keep the view centered
        translateTransform.setX(translateTransform.getX() + shift * scaleTransform.getX() / 2);
    }

    private void shiftNodesVertically(double shift) {
        for (Node child : mainPanel.getChildren()) {
            if (child instanceof DraggableNodeController) {
                DraggableNodeController node = (DraggableNodeController) child;
                node.setLayoutY(node.getLayoutY() + shift);
            }
        }

        // Adjust the translate transform to keep the view centered
        translateTransform.setY(translateTransform.getY() + shift * scaleTransform.getY() / 2);
    }


    // Expand canvas size if nodes are placed beyond current bounds
    public void expandCanvasIfNeeded(DraggableNodeController node) {
        double nodeLeft = node.getLayoutX();
        double nodeTop = node.getLayoutY();
        double nodeRight = node.getLayoutX() + node.getBoundsInParent().getWidth();
        double nodeBottom = node.getLayoutY() + node.getBoundsInParent().getHeight();

        double canvasWidth = mainPanel.getPrefWidth();
        double canvasHeight = mainPanel.getPrefHeight();

        boolean expanded = false;

        // Expand to the right
        if (nodeRight > canvasWidth) {
            mainPanel.setPrefWidth(canvasWidth * 2);
            expanded = true;
        }

        // Expand to the left
        if (nodeLeft < 0) {
            double shift = canvasWidth;
            mainPanel.setPrefWidth(canvasWidth * 2);
            shiftNodesHorizontally(shift);
            expanded = true;
        }

        // Expand downward
        if (nodeBottom > canvasHeight) {
            mainPanel.setPrefHeight(canvasHeight * 2);
            expanded = true;
        }

        // Expand upward
        if (nodeTop < 0) {
            double shift = canvasHeight;
            mainPanel.setPrefHeight(canvasHeight * 2);
            shiftNodesVertically(shift);
            expanded = true;
        }

        if (expanded) {
            // Keep the background color consistent
            mainPanel.setStyle("-fx-background-color: #f8f9fa;");
        }
    }

    private void updateFlowConnectButtonState() {
        //flowConnectButton.setDisable(selectedNode == null);
    }


    public void deactivateToggleButtons() {
        zoomInButton.setSelected(false);
        zoomOutButton.setSelected(false);
        moveButton.setSelected(false);
        flowConnectButton.setSelected(false);
        flowConnectMode = false;
    }


    public void setSelectedNode(DraggableNodeController node) {
        selectedNode = node;
        updateFlowConnectButtonState();
    }

    public void clearSelectedNode() {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
            selectedNode = null;
            updateFlowConnectButtonState();
        }
    }

    public DraggableNodeController getSelectedNode() {
        return selectedNode;
    }

    public boolean isFlowConnectMode() {
        return flowConnectMode;
    }

}
