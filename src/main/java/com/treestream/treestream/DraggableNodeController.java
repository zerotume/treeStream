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
        addDoubleClickHandler();
        addSelectionHandler();
    }

    private void addDragHandlers() {
        this.setOnMousePressed(event -> {
            mouseAnchorX = event.getX();
            mouseAnchorY = event.getY();
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            double offsetX = event.getX() - mouseAnchorX;
            double offsetY = event.getY() - mouseAnchorY;

            // Adjust for scaling
            double scale = this.getParent().getLocalToSceneTransform().getMxx();

            // Update position
            double newLayoutX = this.getLayoutX() + offsetX / scale;
            double newLayoutY = this.getLayoutY() + offsetY / scale;

            this.setLayoutX(newLayoutX);
            this.setLayoutY(newLayoutY);

            // Notify MainController to expand canvas if needed
            mainController.expandCanvasIfNeeded(this);

            event.consume();
        });
    }


    private void addDoubleClickHandler() {
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editName();
            }
            event.consume();
        });
    }

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
            rectangle.setStroke(Color.BLUE); // Indicate selection
        } else {
            rectangle.setStroke(Color.web("#333333")); // Default stroke color
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    private void addSelectionHandler() {
        this.setOnMouseClicked(event -> {
            // Consume the event if in flow connect mode
            if (mainController.isFlowConnectMode()) {
                //mainController.handleNodeClickedForConnection(this);
                event.consume();
                return;
            }

            if (event.getClickCount() == 1) {
                mainController.clearSelectedNode();
                setSelected(true);
                mainController.setSelectedNode(this);
            }
            event.consume();
        });
    }


    private void finishEditing() {
        nameLabel.setText(nameField.getText());
        nameField.setVisible(false);
        nameLabel.setVisible(true);
    }
}
