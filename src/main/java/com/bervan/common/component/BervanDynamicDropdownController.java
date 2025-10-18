package com.bervan.common.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public class BervanDynamicDropdownController extends VerticalLayout implements AutoConfigurableField<String> {
    public final String key;
    private final ComboBox<String> comboBox;
    private boolean isRequired = false;

    public BervanDynamicDropdownController(String key, String label, List<String> values, String initialValue, boolean isRequired) {
        this.key = key;
        this.comboBox = new ComboBox<>(label, values);
        this.isRequired = isRequired;
        comboBox.setRequiredIndicatorVisible(isRequired);
        comboBox.setRequired(isRequired);
        setSpacing(false);
        setValue(initialValue);

        add(comboBox);
        initListener();
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

    private void initListener() {
        comboBox.addValueChangeListener(event -> {
            validate();
        });
    }

    @Override
    public void validate() {
        if (isRequired && comboBox.isEmpty()) {
            comboBox.setInvalid(true);
        }
    }

    @Override
    public boolean isInvalid() {
        return comboBox.isInvalid();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        comboBox.setReadOnly(readOnly);
    }
}
