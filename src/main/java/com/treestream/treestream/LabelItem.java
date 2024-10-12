package com.treestream.treestream;

import javafx.beans.property.SimpleStringProperty;

public class LabelItem {
    private final SimpleStringProperty name = new SimpleStringProperty();

    public LabelItem(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String newName) {
        name.set(newName);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

