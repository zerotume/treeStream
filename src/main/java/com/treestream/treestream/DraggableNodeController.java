package com.treestream.treestream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.IOException;



public class DraggableNodeController extends StackPane {

    @FXML
    private Rectangle rectangle;

    @FXML
    private Label nameLabel;

    @FXML
    private TextField nameField;

    private double mouseAnchorX;
    private double mouseAnchorY;

    private MainController mainController;
    private boolean isSelected = false;

    private static int idCounter = 0;
    private int nodeId;


    public DraggableNodeController(MainController mainController) {
        this.mainController = mainController;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("draggable_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void initialize() {
        addDragHandlers();
        //addDoubleClickHandler(); // merged
        // addSelectionHandler(); // merged
        addMouseClickHandler(); // if we have multiple click handlers, only the last one takes effect
        this.setFocusTraversable(true); // Make the node focus traversable
    }

    private void addDragHandlers() {
        this.setOnMousePressed(event -> {
            mouseAnchorX = event.getX();
            mouseAnchorY = event.getY();
        });

        this.setOnMouseDragged(event -> {
            double newX = this.getLayoutX() + event.getX() - mouseAnchorX;
            double newY = this.getLayoutY() + event.getY() - mouseAnchorY;

            this.setLayoutX(newX);
            this.setLayoutY(newY);

            event.consume();
        });

        this.setOnMouseReleased(event -> {
            // After the drag is complete, check if expansion is needed
            mainController.expandCanvasIfNeeded(this);
            event.consume();
        });
    }


// merged:
//    private void addDoubleClickHandler() {
//        this.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2) {
//                editName();
//            }
//            event.consume();
//        });
//    }

    public void editName() {
        nameLabel.setVisible(false);
        nameField.setVisible(true);
        nameField.requestFocus();

        nameField.setOnAction(e -> finishEditing());

        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                finishEditing();
            }
        });
    }

    public void setSelected(boolean select) {
        isSelected = select;
        if (isSelected) {
            if (!getStyleClass().contains("selected")) {
                getStyleClass().add("selected");
            }
        } else {
            getStyleClass().remove("selected");
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    private void addMouseClickHandler() {
        this.setOnMouseClicked(event -> {
            if (mainController.isFlowConnectMode()) {
                mainController.handleNodeClickedForConnection(this);
                event.consume();
                return;
            }

            if (event.getClickCount() == 2) {
                editName();
                event.consume();
            } else if (event.getClickCount() == 1) {
                mainController.clearSelectedNode();
                setSelected(true);
                mainController.setSelectedNode(this);
                this.requestFocus();
                event.consume();
            }
        });
    }


    // merged:
//    private void addSelectionHandler() {
//        this.setOnMouseClicked(event -> {
//            if (mainController.isFlowConnectMode()) {
//                mainController.handleNodeClickedForConnection(this);
//                event.consume();
//                return;
//            }
//
//            if (event.getClickCount() == 1) {
//                mainController.clearSelectedNode();
//                setSelected(true);
//                mainController.setSelectedNode(this);
//                this.requestFocus(); // Request focus to ensure focus change to exit edit mode
//            }
//            event.consume();
//        });
//    }



    private void finishEditing() {
        nameLabel.setText(nameField.getText());
        nameField.setVisible(false);
        nameLabel.setVisible(true);
        if (isSelected) {
            setSelected(true);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DraggableNodeController)) return false;
        DraggableNodeController other = (DraggableNodeController) obj;
        // Compare based on unique identifier or other criteria
        return this.uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
}
