package com.treestream.treestream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;


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

    @FXML
    private ListView<LabelItem> labelsListView;

    @FXML
    private Button createLabelButton;

    @FXML
    private Button deleteLabelButton;

    @FXML
    private Button attachLabelButton;

    @FXML
    private Button detachLabelButton;

    @FXML
    private VBox attachedLabelsSection;

    @FXML
    private ListView<LabelItem> attachedLabelsListView;


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

    //private Map<DraggableNodeController, List<DraggableNodeController>> connections = new HashMap<>();
    private List<Arrow> arrows = new ArrayList<>();
    private Arrow selectedArrow = null;

    private Graph graph = new Graph();

    private ObservableList<LabelItem> labels = FXCollections.observableArrayList();
    private LabelItem selectedLabel = null;

    @FXML
    private void initialize() {
        // Initialize transforms
        scaleTransform = new Scale(scaleValue, scaleValue, 0, 0);
        translateTransform = new Translate();

        mainPanel.setFocusTraversable(true);

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
                newScene.setOnKeyPressed(this::handleKeyPressed);
            }
        });

        mainPanel.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getTarget() == mainPanel) {
                clearSelectedNode();
                clearSelectedArrow();
                deactivateFlowConnectMode();
                // do not consume - so it could exit the edit mode?
                //event.consume();
                mainPanel.requestFocus();
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

        labelsListView.setItems(labels);

        // Handle label selection
        labelsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedLabel = newSelection;
            updateLabelButtonsState();
        });

        // Set cell factory for editable labels
        labelsListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<LabelItem>() {
            @Override
            public String toString(LabelItem label) {
                return label.getName();
            }

            @Override
            public LabelItem fromString(String string) {
                selectedLabel.setName(string);
                return selectedLabel;
            }
        }));

        // Handle label click events
        labelsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedLabel != null) {
                labelsListView.edit(labelsListView.getSelectionModel().getSelectedIndex());
            }
        });

        // Handle clicks outside labels to unselect
        labelsSection.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getTarget() == labelsSection) {
                labelsListView.getSelectionModel().clearSelection();
                selectedLabel = null;
                updateLabelButtonsState();
                labelsListView.edit(-1); // Exit edit mode
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

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            if (selectedArrow != null) {
                deleteArrow(selectedArrow);
                selectedArrow = null;
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            deactivateToggleButtons();
        }
    }



    private void deleteArrow(Arrow arrow) {
        mainPanel.getChildren().remove(arrow);
        arrows.remove(arrow);
        // Remove the edge from the graph
        graph.removeEdge(arrow.getSourceNode(), arrow.getTargetNode());
    }

    private void deactivateFlowConnectMode() {
        flowConnectButton.setSelected(false);
        flowConnectMode = false;
    }


    @FXML
    private void handleAddObject() {
        // Create a new DraggableNodeController instance
        DraggableNodeController node = new DraggableNodeController(this);
        graph.addNode(node);

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

    // do the delete into a handler later - leaving it here for now
    public void deleteNode(DraggableNodeController node) {
        // Remove from mainPanel
        mainPanel.getChildren().remove(node);
        // Remove from graph
        graph.removeNode(node);
        // Also remove any arrows connected to this node
        // ...
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
            deactivateFlowConnectMode();
            return;
        }

        if (selectedNode == targetNode) {
            // Self-connection detected
            showError("Connect failed: self-pointing detected");
            clearSelectedNode();
            deactivateFlowConnectMode();
            return;
        }

        // Check for existing connection to prevent cycles
        // commented out since we have graph.createsCycle
        /*List<DraggableNodeController> connectedNodes = connections.getOrDefault(targetNode, new ArrayList<>());
        if (connectedNodes.contains(selectedNode)) {
            showError("Connect failed: self-pointing detected");
            clearSelectedNode();
            deactivateFlowConnectMode();
            return;
        }*/

        // Create the connection
        createConnection(selectedNode, targetNode);

        // Clear selection and flow connect mode
        clearSelectedNode();
        deactivateFlowConnectMode();
    }

    private void createConnection(DraggableNodeController sourceNode, DraggableNodeController targetNode) {
        // Check for cycles
        if (graph.createsCycle(sourceNode, targetNode)) {
            showError("Connect failed: adding this connection creates a cycle.");
            return;
        }

        // Add the edge to the graph
        graph.addEdge(sourceNode, targetNode);

        // Create an arrow between sourceNode and targetNode
        Arrow arrow = new Arrow(this, sourceNode, targetNode);

        // Add the arrow to the mainPanel
        mainPanel.getChildren().add(0, arrow); // Add behind nodes

        // Store the arrow
        arrows.add(arrow);
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            } else if (child instanceof Arrow) {
                // Arrows will adjust automatically if bound to node positions
            }
        }

        // Adjust the translate transform if necessary
        // translateTransform.setX(translateTransform.getX() + shift * scaleTransform.getX());
    }


    private void shiftNodesVertically(double shift) {
        for (Node child : mainPanel.getChildren()) {
            if (child instanceof DraggableNodeController) {
                DraggableNodeController node = (DraggableNodeController) child;
                node.setLayoutY(node.getLayoutY() + shift);
            } else if (child instanceof Arrow) {
                // Arrows will adjust automatically if bound to node positions
            }
        }

        // Adjust the translate transform if necessary
        // translateTransform.setY(translateTransform.getY() + shift * scaleTransform.getY());
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

        // Variables to store old and new canvas sizes
        double oldCanvasWidth = canvasWidth;
        double oldCanvasHeight = canvasHeight;

        // Expand to the right
        if (nodeRight > canvasWidth) {
            mainPanel.setPrefWidth(nodeRight + 100); // Expand just enough to include the node plus some padding
            expanded = true;
        }

        // Expand to the left
        if (nodeLeft < 0) {
            double shift = -nodeLeft + 100; // Amount to shift nodes to the right
            mainPanel.setPrefWidth(canvasWidth + shift);
            shiftNodesHorizontally(shift);
            expanded = true;
        }

        // Expand downward
        if (nodeBottom > canvasHeight) {
            mainPanel.setPrefHeight(nodeBottom + 100); // Expand just enough to include the node plus some padding
            expanded = true;
        }

        // Expand upward
        if (nodeTop < 0) {
            double shift = -nodeTop + 100; // Amount to shift nodes downward
            mainPanel.setPrefHeight(canvasHeight + shift);
            shiftNodesVertically(shift);
            expanded = true;
        }

        if (expanded) {
            // Keep the background color consistent
            mainPanel.setStyle("-fx-background-color: #f8f9fa;");
            // Adjust the viewport to center on the node
            centerViewportOnNode(node);
        }
    }

    private void centerViewportOnNode(DraggableNodeController node) {
        // Calculate the node's position relative to the content
        double nodeCenterX = node.getLayoutX() + node.getBoundsInParent().getWidth() / 2;
        double nodeCenterY = node.getLayoutY() + node.getBoundsInParent().getHeight() / 2;

        double contentWidth = mainPanel.getPrefWidth();
        double contentHeight = mainPanel.getPrefHeight();

        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        // Calculate the scroll values needed to center the node
        double hValue = (nodeCenterX - viewportWidth / 2) / (contentWidth - viewportWidth);
        double vValue = (nodeCenterY - viewportHeight / 2) / (contentHeight - viewportHeight);

        // Clamp the values between 0 and 1
        hValue = Math.max(0, Math.min(hValue, 1));
        vValue = Math.max(0, Math.min(vValue, 1));

        // Update the scroll values after the layout pass
        double finalHValue = hValue;
        double finalVValue = vValue;
        Platform.runLater(() -> {
            scrollPane.setHvalue(finalHValue);
            scrollPane.setVvalue(finalVValue);
        });
    }





    private void updateFlowConnectButtonState() {
        flowConnectButton.setDisable(selectedNode == null);
    }


    public void deactivateToggleButtons() {
        zoomInButton.setSelected(false);
        zoomOutButton.setSelected(false);
        moveButton.setSelected(false);
        deactivateFlowConnectMode();
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

    public void setSelectedArrow(Arrow arrow) {
        selectedArrow = arrow;
    }

    public void clearSelectedArrow() {
        if (selectedArrow != null) {
            selectedArrow.setSelected(false);
            selectedArrow = null;
        }
    }


    public DraggableNodeController getSelectedNode() {
        return selectedNode;
    }

    public boolean isFlowConnectMode() {
        return flowConnectMode;
    }

}
