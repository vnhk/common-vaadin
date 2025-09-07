package com.bervan.common.component;

import com.vaadin.flow.component.textfield.NumberField;

public class BervanLongField extends NumberField implements AutoConfigurableField<Double> {
    public BervanLongField() {
        super();
    }

    public BervanLongField(String label) {
        this();
        this.setLabel(label);
    }

    public BervanLongField(String label, String placeholder) {
        this(label);
        this.setPlaceholder(placeholder);
    }

    public void setMin(long min) {
        super.setMin((double)min);
    }

    public void setMax(long max) {
        super.setMax((double)max);
    }

    public void setStep(long step) {
        if (step <= 0) {
            throw new IllegalArgumentException("The step cannot be less or equal to zero.");
        } else {
            super.setStep((double)step);
        }
    }

    @Override
    public void setValue(Double obj) {
        super.setValue((double) obj.longValue());
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
    public Double getValue() {
        return super.getValue();
    }
}
