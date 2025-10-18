package com.bervan.common.component;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.shared.Registration;

public class BervanTextArea extends TextArea implements AutoConfigurableField<String> {
    public BervanTextArea() {
    }

    public BervanTextArea(String label, boolean isRequired, int minLength, int maxLength) {
        super(label);
        setRequiredIndicatorVisible(isRequired);
        setMinLength(minLength);
        setMaxLength(maxLength);
        setRequired(isRequired);
    }

    public BervanTextArea(String label, String placeholder) {
        super(label, placeholder);
    }

    public BervanTextArea(String label, String initialValue, String placeholder) {
        super(label, initialValue, placeholder);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<TextArea, String>> listener) {
        validate();
        return super.addValueChangeListener(listener);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
    }
}
