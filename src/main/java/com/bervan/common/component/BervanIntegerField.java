package com.bervan.common.component;

import com.vaadin.flow.component.textfield.IntegerField;

public class BervanIntegerField extends IntegerField implements AutoConfigurableField<Integer> {
    public BervanIntegerField() {
    }

    public BervanIntegerField(String label) {
        super(label);
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
}
