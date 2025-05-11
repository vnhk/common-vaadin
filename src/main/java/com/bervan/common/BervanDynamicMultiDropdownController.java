package com.bervan.common;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

public class BervanDynamicMultiDropdownController extends VerticalLayout implements AutoConfigurableField<List<String>> {

    public final String key;
    private final MultiSelectComboBox<String> multiSelectComboBox;

    public BervanDynamicMultiDropdownController(String key, String label, List<String> availableCategories, List<String> selectedValues) {
        this.key = key;
        this.multiSelectComboBox = new MultiSelectComboBox<>(label);
        setSpacing(false);
        multiSelectComboBox.setMinWidth("200px");
        multiSelectComboBox.setMaxWidth("600px");

        multiSelectComboBox.setItems(availableCategories);
        multiSelectComboBox.setAllowCustomValue(true);

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

    @Override
    public void setReadOnly(boolean readOnly) {
        multiSelectComboBox.setReadOnly(readOnly);
    }
}