package com.bervan.common;

import com.vaadin.flow.component.checkbox.Checkbox;

public class BervanBooleanField extends Checkbox implements AutoConfigurableField<Boolean> {
    public BervanBooleanField() {
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
}
