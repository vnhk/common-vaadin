package com.bervan.common.component;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.LocalDate;

public class BervanDatePicker extends HorizontalLayout implements AutoConfigurableField<LocalDate> {
    private DatePicker datePicker = new DatePicker();
    private boolean isRequired = false;

    public BervanDatePicker() {
        add(datePicker);
    }

    public BervanDatePicker(String label, boolean isRequired) {
        datePicker = new DatePicker(label);
        this.isRequired = isRequired;
        datePicker.setRequiredIndicatorVisible(isRequired);
        datePicker.setRequired(isRequired);
        add(datePicker);
        initListener();
    }

    public BervanDatePicker(String label, LocalDate localDate) {
        datePicker = new DatePicker(label);
        setValue(localDate);
        add(datePicker);
        initListener();
    }

    public BervanDatePicker(LocalDate localDate, boolean isRequired) {
        setValue(localDate);
        this.isRequired = isRequired;
        datePicker.setRequiredIndicatorVisible(isRequired);
        datePicker.setRequired(isRequired);
        add(datePicker);
        initListener();
    }

    public void addValueChangeListener(HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>> eventChange) {
        datePicker.addValueChangeListener(eventChange);
    }

    private void initListener() {
        datePicker.addValueChangeListener(event -> {
            validate();
        });
    }

    @Override
    public LocalDate getValue() {
        return datePicker.getValue();
    }

    @Override
    public void setValue(LocalDate obj) {
        datePicker.setValue(obj);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    public void setLabel(String label) {
        datePicker.setLabel(label);
    }


    @Override
    public void validate() {
        if (isRequired) {
            datePicker.setInvalid(datePicker.isEmpty());
        }
    }

    @Override
    public boolean isInvalid() {
        return datePicker.isInvalid();
    }
}
