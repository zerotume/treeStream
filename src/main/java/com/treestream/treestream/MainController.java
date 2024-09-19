package com.treestream.treestream;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class MainController {

    @FXML
    private Pane mainPanel;

    @FXML
    private void handleAddObject() {
        // Create a new DraggableNodeController instance
        DraggableNodeController node = new DraggableNodeController();
        node.setLayoutX(50);
        node.setLayoutY(50);
        mainPanel.getChildren().add(node);
        node.editName(); // Focus on the TextField for editing
    }
}
