package com.bervan.common;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.Collection;

public class BervanComboBox<T> extends ComboBox<T> implements AutoConfigurableField<T> {
    public BervanComboBox(int pageSize) {
        super(pageSize);
    }

    public BervanComboBox() {
    }

    public BervanComboBox(String label) {
        super(label);
    }

    public BervanComboBox(String label, Collection<T> items) {
        super(label, items);
    }

    public BervanComboBox(String label, T... items) {
        super(label, items);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }
}
