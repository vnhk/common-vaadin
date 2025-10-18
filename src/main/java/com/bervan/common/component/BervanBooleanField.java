package com.bervan.common.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.shared.Registration;

public class BervanBooleanField extends Checkbox implements AutoConfigurableField<Boolean> {
    public BervanBooleanField(String label, boolean isRequired) {
        this.setLabel(label);
        this.setRequiredIndicatorVisible(isRequired);
    }

    @Override
    public void setValue(Boolean obj) {
        super.setValue(obj);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
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
