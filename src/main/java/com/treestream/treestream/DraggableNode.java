package com.treestream.treestream;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class DraggableNode extends StackPane {

    private Rectangle rectangle;
    private Label nameLabel;
    private TextField nameField;

    private double mouseAnchorX;
    private double mouseAnchorY;

    public DraggableNode() {
        // Initialize components
        rectangle = new Rectangle(120, 60);
        rectangle.getStyleClass().add("btn-primary"); // BootstrapFX style

        nameLabel = new Label("new system");
        nameLabel.getStyleClass().add("text-white");
        nameField = new TextField("new system");
        nameField.setVisible(false);


        // Add event handlers
        addDragHandlers();
        addDoubleClickHandler();

        // Add components to the StackPane
        this.getChildren().addAll(rectangle, nameLabel, nameField);
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