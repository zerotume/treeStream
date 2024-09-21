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
            mouseAnchorX = event.getX();
            mouseAnchorY = event.getY();
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            double offsetX = event.getX() - mouseAnchorX;
            double offsetY = event.getY() - mouseAnchorY;

            // Adjust for scaling
            double scale = this.getParent().getLocalToSceneTransform().getMxx();

            this.setLayoutX(this.getLayoutX() + offsetX / scale);
            this.setLayoutY(this.getLayoutY() + offsetY / scale);

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

    private void finishEditing() {
        nameLabel.setText(nameField.getText());
        nameField.setVisible(false);
        nameLabel.setVisible(true);
    }
}
