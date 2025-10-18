package com.bervan.common.component;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.shared.Registration;

public class BervanIntegerField extends IntegerField implements AutoConfigurableField<Integer> {
    public BervanIntegerField() {
    }

    public BervanIntegerField(String label, boolean required, Integer min, Integer max) {
        super(label);
        setRequiredIndicatorVisible(required);
        setMin(min);
        setMax(max);
        setRequired(required);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<IntegerField, Integer>> listener) {
        validate();
        return super.addValueChangeListener(listener);
    }

    public BervanIntegerField(String label, String placeholder) {
        super(label, placeholder);
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
    public void validate() {
        super.validate();
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid();
    }
}
