package com.bervan.common;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.LocalDate;
import java.time.LocalTime;

public class BervanDatePicker extends HorizontalLayout implements AutoConfigurableField<LocalDate> {
    private DatePicker datePicker = new DatePicker();

    public BervanDatePicker() {
        add(datePicker);
    }

    public BervanDatePicker(String label) {
        datePicker = new DatePicker(label);
        add(datePicker);
    }

    public BervanDatePicker(String label, LocalDate localDate) {
        datePicker = new DatePicker(label);
        setValue(localDate);
        add(datePicker);
    }

    public BervanDatePicker(LocalDate localDate) {
        setValue(localDate);
        add(datePicker);
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
}
