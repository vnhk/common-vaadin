package com.bervan.common;

import com.vaadin.flow.component.textfield.TextArea;

public class BervanTextArea extends TextArea implements AutoConfigurableField<String> {
    public BervanTextArea() {
    }

    public BervanTextArea(String label) {
        super(label);
    }

    public BervanTextArea(String label, String placeholder) {
        super(label, placeholder);
    }

    public BervanTextArea(String label, String initialValue, String placeholder) {
        super(label, initialValue, placeholder);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }
}
