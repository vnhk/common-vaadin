package com.bervan.common.component;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

public class BervanTextField extends TextField implements AutoConfigurableField<String> {
    public BervanTextField() {
    }

    public BervanTextField(String label) {
        super(label);
    }

    public BervanTextField(String label, String placeholder) {
        super(label, placeholder);
    }

    public BervanTextField(String label, String initialValue, String placeholder) {
        super(label, initialValue, placeholder);
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
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
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
