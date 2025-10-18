package com.bervan.common.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.LocalTime;

public class BervanTimePicker extends HorizontalLayout implements AutoConfigurableField<LocalTime> {
    private TimePicker timePicker = new TimePicker();
    private boolean isRequired = false;

    public BervanTimePicker() {
        add(timePicker);
        initListener();
    }

    public BervanTimePicker(String label, boolean isRequired) {
        timePicker = new TimePicker(label);
        timePicker.setRequiredIndicatorVisible(isRequired);
        this.isRequired = isRequired;
        add(timePicker);
        initListener();
    }

    public BervanTimePicker(String label, LocalTime initialTime) {
        timePicker = new TimePicker(label);
        setValue(initialTime);
        add(timePicker);
        initListener();
    }

    public BervanTimePicker(LocalTime initialTime) {
        setValue(initialTime);
        add(timePicker);
        initListener();
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

    private void initListener() {
        timePicker.addValueChangeListener(event -> {
            validate();
        });
    }

    @Override
    public void validate() {
        if(isRequired) {
            timePicker.setInvalid(timePicker.isEmpty());
        }
    }

    @Override
    public boolean isInvalid() {
        return timePicker.isInvalid();
    }
}
