package com.bervan.common;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class BervanDynamicDropdownController extends VerticalLayout implements AutoConfigurableField<String> {
    public final String key;
    private final ComboBox<String> comboBox;

    public BervanDynamicDropdownController(String key, String label, List<String> values, String initialValue) {
        this.key = key;
        this.comboBox = new ComboBox<>(label, values);
        setSpacing(false);
        setValue(initialValue);

        add(comboBox);
    }

    @Override
    public String getValue() {
        return comboBox.getValue();
    }

    @Override
    public void setValue(String obj) {
        comboBox.setValue(obj);
    }

    @Override
    public void setWidthFull() {
        comboBox.setWidthFull();
        super.setWidthFull();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        comboBox.setReadOnly(readOnly);
    }
}
