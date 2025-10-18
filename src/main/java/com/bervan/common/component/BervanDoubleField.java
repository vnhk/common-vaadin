package com.bervan.common.component;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.shared.Registration;

public class BervanDoubleField extends NumberField implements AutoConfigurableField<Double> {
    public BervanDoubleField() {
    }

    public BervanDoubleField(String label, boolean isRequired, Integer min, Integer max) {
        super(label);
        setRequiredIndicatorVisible(isRequired);
        setMin(min);
        setMax(max);
        setRequired(isRequired);
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
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<NumberField, Double>> listener) {
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
