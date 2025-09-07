package com.bervan.common.component;

import com.vaadin.flow.component.textfield.NumberField;

public class BervanDoubleField extends NumberField implements AutoConfigurableField<Double> {
    public BervanDoubleField() {
    }

    public BervanDoubleField(String label) {
        super(label);
    }

    public BervanDoubleField(String label, String placeholder) {
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
}
