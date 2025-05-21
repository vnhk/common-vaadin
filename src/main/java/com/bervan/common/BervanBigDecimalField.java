package com.bervan.common;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;

import java.math.BigDecimal;

public class BervanBigDecimalField extends BigDecimalField implements AutoConfigurableField<BigDecimal> {
    public BervanBigDecimalField() {
    }

    public BervanBigDecimalField(String label) {
        super(label);
    }

    public BervanBigDecimalField(String label, String placeholder) {
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
