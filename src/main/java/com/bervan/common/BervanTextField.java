package com.bervan.common;

import com.vaadin.flow.component.textfield.TextField;

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
}
