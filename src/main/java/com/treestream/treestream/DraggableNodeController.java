package com.treestream.treestream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
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

    public DraggableNodeController() {
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
    }

    private void addDragHandlers() {
        this.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX() - this.getLayoutX();
            mouseAnchorY = event.getSceneY() - this.getLayoutY();
        });

        this.setOnMouseDragged(event -> {
            this.setLayoutX(event.getSceneX() - mouseAnchorX);
            this.setLayoutY(event.getSceneY() - mouseAnchorY);
        });
    }

    private void addDoubleClickHandler() {
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editName();
            }
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

    private void finishEditing() {
        nameLabel.setText(nameField.getText());
        nameField.setVisible(false);
        nameLabel.setVisible(true);
    }
}
