package com.bervan.common.component;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.shared.Registration;

import java.math.BigDecimal;

public class BervanBigDecimalField extends BigDecimalField implements AutoConfigurableField<BigDecimal> {
    public BervanBigDecimalField() {
    }

    public BervanBigDecimalField(String label, boolean isRequired) {
        super(label);
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
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<BigDecimalField, BigDecimal>> listener) {
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
