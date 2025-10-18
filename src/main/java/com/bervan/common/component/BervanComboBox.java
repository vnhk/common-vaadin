package com.bervan.common.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;

public class BervanComboBox<T> extends ComboBox<T> implements AutoConfigurableField<T> {
    public BervanComboBox(int pageSize) {
        super(pageSize);
    }

    public BervanComboBox() {
    }

    public BervanComboBox(String label, boolean isRequired) {
        super(label);
        this.setRequiredIndicatorVisible(isRequired);
        this.setRequired(isRequired);
    }

    public BervanComboBox(String label, Collection<T> items, boolean isRequired) {
        super(label, items);
        this.setRequiredIndicatorVisible(isRequired);
        this.setRequired(isRequired);
    }

    public BervanComboBox(Collection<T> items) {
        super();
        setItems(items);
    }

    public BervanComboBox(String label, boolean isRequired, T... items) {
        super(label, items);
        this.setRequiredIndicatorVisible(isRequired);
        this.setRequired(isRequired);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        validate();
        return super.addValueChangeListener(listener);
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid();
    }
}
