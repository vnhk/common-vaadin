package com.bervan.common.component;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BervanDynamicMultiDropdownController extends VerticalLayout implements AutoConfigurableField<List<String>> {
    public final String key;
    private final MultiSelectComboBox<String> multiSelectComboBox;
    private boolean isRequired;

    public BervanDynamicMultiDropdownController(String key, String label, Collection<String> availableValues, Collection<String> selectedValues, boolean isRequired) {
        this.key = key;
        this.multiSelectComboBox = new MultiSelectComboBox<>(label);
        setSpacing(false);
        setPadding(false);
        multiSelectComboBox.setMinWidth("200px");
        multiSelectComboBox.setMaxWidth("600px");

        multiSelectComboBox.setItems(availableValues);
        multiSelectComboBox.setAllowCustomValue(true);
        this.isRequired = isRequired;
        multiSelectComboBox.setRequiredIndicatorVisible(isRequired);
        multiSelectComboBox.setRequired(isRequired);
        multiSelectComboBox.setClearButtonVisible(true);//?

        if (selectedValues != null) {
            multiSelectComboBox.select(selectedValues);
        }

        multiSelectComboBox.addCustomValueSetListener(e -> {
            String newCategory = e.getDetail();
            multiSelectComboBox.select(newCategory);
        });

        multiSelectComboBox.getElement().getStyle().set("--vaadin-combo-box-overlay-max-height", "300px");

        setWidthFull();
        add(multiSelectComboBox);
        initListener();
    }

    @Override
    public List<String> getValue() {
        return new ArrayList<>(multiSelectComboBox.getSelectedItems());
    }

    @Override
    public void setValue(List<String> categories) {
        multiSelectComboBox.deselectAll();
        if (categories != null) {
            multiSelectComboBox.select(categories);
        }
    }

    @Override
    public void setWidthFull() {
        multiSelectComboBox.setWidthFull();
        super.setWidthFull();
    }

    private void initListener() {
        multiSelectComboBox.addValueChangeListener(event -> {
            validate();
        });
    }

    @Override
    public void validate() {
        if (isRequired && multiSelectComboBox.isEmpty()) {
            multiSelectComboBox.setInvalid(true);
        } else {
            multiSelectComboBox.setInvalid(false);
        }
    }

    @Override
    public boolean isInvalid() {
        return multiSelectComboBox.isInvalid();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        multiSelectComboBox.setReadOnly(readOnly);
    }
}