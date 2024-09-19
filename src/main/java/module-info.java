module com.treestream.treestream {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;

    opens com.treestream.treestream to javafx.fxml;
    exports com.treestream.treestream;
}