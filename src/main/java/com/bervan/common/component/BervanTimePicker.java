package com.bervan.common.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.LocalTime;

public class BervanTimePicker extends HorizontalLayout implements AutoConfigurableField<LocalTime> {
    private TimePicker timePicker = new TimePicker();

    public BervanTimePicker() {
        add(timePicker);
    }

    public BervanTimePicker(String label) {
        timePicker = new TimePicker(label);
        add(timePicker);
    }

    public BervanTimePicker(String label, LocalTime initialTime) {
        timePicker = new TimePicker(label);
        setValue(initialTime);
        add(timePicker);
    }

    public BervanTimePicker(LocalTime initialTime) {
        setValue(initialTime);
        add(timePicker);
    }

    @Override
    public LocalTime getValue() {
        return timePicker.getValue();
    }

    @Override
    public void setValue(LocalTime obj) {
        timePicker.setValue(obj);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    public void setLabel(String label) {
        timePicker.setLabel(label);
    }
}
