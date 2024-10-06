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
                -20.0, -15.0,
                -20.0, 15.0
        );

        line.setStrokeWidth(7);
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
    }

    private void updateLineBindings() {
        // Line start point bindings (same as before)
        line.startXProperty().bind(Bindings.createDoubleBinding(
                () -> sourceNode.getLayoutX() + sourceNode.getWidth() / 2,
                sourceNode.layoutXProperty(), sourceNode.widthProperty()));

        line.startYProperty().bind(Bindings.createDoubleBinding(
                () -> sourceNode.getLayoutY() + sourceNode.getHeight() / 2,
                sourceNode.layoutYProperty(), sourceNode.heightProperty()));

        // Line end point bindings with adjusted calculations
        line.endXProperty().bind(Bindings.createDoubleBinding(
                this::calculateAdjustedEndX,
                sourceNode.layoutXProperty(), sourceNode.layoutYProperty(),
                sourceNode.widthProperty(), sourceNode.heightProperty(),
                targetNode.layoutXProperty(), targetNode.layoutYProperty(),
                targetNode.widthProperty(), targetNode.heightProperty()
        ));

        line.endYProperty().bind(Bindings.createDoubleBinding(
                this::calculateAdjustedEndY,
                sourceNode.layoutXProperty(), sourceNode.layoutYProperty(),
                sourceNode.widthProperty(), sourceNode.heightProperty(),
                targetNode.layoutXProperty(), targetNode.layoutYProperty(),
                targetNode.widthProperty(), targetNode.heightProperty()
        ));
    }

    private double calculateAdjustedEndX() {
        double sourceCenterX = sourceNode.getLayoutX() + sourceNode.getWidth() / 2;
        double sourceCenterY = sourceNode.getLayoutY() + sourceNode.getHeight() / 2;
        double targetCenterX = targetNode.getLayoutX() + targetNode.getWidth() / 2;
        double targetCenterY = targetNode.getLayoutY() + targetNode.getHeight() / 2;

        double deltaX = targetCenterX - sourceCenterX;
        double deltaY = targetCenterY - sourceCenterY;

        double dx = deltaX;
        double dy = deltaY;

        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);

        // Handle zero-length lines (nodes at the same position)
        if (absDx == 0 && absDy == 0) {
            return targetCenterX;
        }

        double length = Math.hypot(dx, dy);
        double unitDx = dx / length;
        double unitDy = dy / length;

        double halfWidth = targetNode.getWidth() / 2;
        double halfHeight = targetNode.getHeight() / 2;

        double scaleX = absDx > 0 ? halfWidth / absDx : Double.POSITIVE_INFINITY;
        double scaleY = absDy > 0 ? halfHeight / absDy : Double.POSITIVE_INFINITY;

        double scale = Math.min(scaleX, scaleY);

        double offsetX = unitDx * scale * length;
        return targetCenterX - offsetX;
    }

    private double calculateAdjustedEndY() {
        double sourceCenterX = sourceNode.getLayoutX() + sourceNode.getWidth() / 2;
        double sourceCenterY = sourceNode.getLayoutY() + sourceNode.getHeight() / 2;
        double targetCenterX = targetNode.getLayoutX() + targetNode.getWidth() / 2;
        double targetCenterY = targetNode.getLayoutY() + targetNode.getHeight() / 2;

        double deltaX = targetCenterX - sourceCenterX;
        double deltaY = targetCenterY - sourceCenterY;

        double dx = deltaX;
        double dy = deltaY;

        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);

        // Handle zero-length lines
        if (absDx == 0 && absDy == 0) {
            return targetCenterY;
        }

        double length = Math.hypot(dx, dy);
        double unitDx = dx / length;
        double unitDy = dy / length;

        double halfWidth = targetNode.getWidth() / 2;
        double halfHeight = targetNode.getHeight() / 2;

        double scaleX = absDx > 0 ? halfWidth / absDx : Double.POSITIVE_INFINITY;
        double scaleY = absDy > 0 ? halfHeight / absDy : Double.POSITIVE_INFINITY;

        double scale = Math.min(scaleX, scaleY);

        double offsetY = unitDy * scale * length;
        return targetCenterY - offsetY;
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
