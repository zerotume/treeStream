package com.treestream.treestream;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Arrow extends Group {

    private Line line;
    private Polygon arrowHead;
    private DraggableNodeController sourceNode;
    private DraggableNodeController targetNode;
    private MainController mainController;
    private boolean isSelected = false;

    public Arrow(MainController mainController, DraggableNodeController sourceNode, DraggableNodeController targetNode) {
        this.mainController = mainController;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        line = new Line();
        arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
                0.0, 0.0,
                -20.0, -15.0, // Increased size
                -20.0, 15.0
        );

        line.setStrokeWidth(2);
        line.setStroke(Color.BLACK);

        arrowHead.setFill(Color.BLACK);
        arrowHead.setStroke(Color.BLACK);
        arrowHead.setStrokeWidth(1);

        // Bind line start and end points to node positions
        updateLineBindings();

        // Position and rotate arrowhead
        updateArrowheadBindings();

        this.getChildren().addAll(line, arrowHead);

        addSelectionHandler();

//        line.endXProperty().addListener((obs, oldVal, newVal) -> {
//            System.out.println("Line endX: " + newVal);
//        });
//
//        arrowHead.layoutXProperty().addListener((obs, oldVal, newVal) -> {
//            System.out.println("arrow endX: " + newVal);
//        });
    }

    private void updateLineBindings() {
        line.startXProperty().bind(Bindings.createDoubleBinding(() ->
                        sourceNode.getLayoutX() + sourceNode.getWidth() / 2,
                sourceNode.layoutXProperty(), sourceNode.widthProperty()));

        line.startYProperty().bind(Bindings.createDoubleBinding(() ->
                        sourceNode.getLayoutY() + sourceNode.getHeight() / 2,
                sourceNode.layoutYProperty(), sourceNode.heightProperty()));

        line.endXProperty().bind(Bindings.createDoubleBinding(() ->
                        targetNode.getLayoutX() + targetNode.getWidth() / 2,
                targetNode.layoutXProperty(), targetNode.widthProperty()));

        line.endYProperty().bind(Bindings.createDoubleBinding(() ->
                        targetNode.getLayoutY() + targetNode.getHeight() / 2,
                targetNode.layoutYProperty(), targetNode.heightProperty()));
    }

    private void updateArrowheadBindings() {
        // Position arrowhead at the end of the line
        arrowHead.layoutXProperty().bind(line.endXProperty());
        arrowHead.layoutYProperty().bind(line.endYProperty());

        // Rotate arrowhead to point along the line
        DoubleBinding angle = Bindings.createDoubleBinding(() -> {
            double deltaX = line.getEndX() - line.getStartX();
            double deltaY = line.getEndY() - line.getStartY();
            return Math.toDegrees(Math.atan2(deltaY, deltaX));
        }, line.startXProperty(), line.startYProperty(), line.endXProperty(), line.endYProperty());

        arrowHead.rotateProperty().bind(angle);
    }

    private void addSelectionHandler() {
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                mainController.clearSelectedArrow();
                setSelected(true);
                mainController.setSelectedArrow(this);
                event.consume();
            }
        });
    }

    public void setSelected(boolean select) {
        isSelected = select;
        if (isSelected) {
            line.setStroke(Color.BLUE);
            arrowHead.setFill(Color.BLUE);
            arrowHead.setStroke(Color.BLUE);
        } else {
            line.setStroke(Color.BLACK);
            arrowHead.setFill(Color.BLACK);
            arrowHead.setStroke(Color.BLACK);
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public DraggableNodeController getSourceNode() {
        return sourceNode;
    }

    public DraggableNodeController getTargetNode() {
        return targetNode;
    }
}
