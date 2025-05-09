package com.bervan.common;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BervanDateTimePicker extends HorizontalLayout implements AutoConfigurableField<LocalDateTime> {
    private DatePicker datePicker = new DatePicker();
    private TimePicker timePicker = new TimePicker();

    public BervanDateTimePicker() {
        add(datePicker, timePicker);
    }

    public BervanDateTimePicker(String label) {
        datePicker = new DatePicker(label);
        add(datePicker, timePicker);
    }

    public BervanDateTimePicker(String label, LocalDateTime initialDateTime) {
        datePicker = new DatePicker(label);
        setValue(initialDateTime);
        add(datePicker, timePicker);
    }

    public BervanDateTimePicker(LocalDateTime initialDateTime) {
        setValue(initialDateTime);
        add(datePicker, timePicker);
    }

    @Override
    public LocalDateTime getValue() {
        LocalDate localDate = datePicker.getValue();
        LocalTime localTime = timePicker.getValue();

        if (localDate == null) {
            return null;
        }

        LocalTime time = (localTime != null) ? localTime : LocalTime.MIDNIGHT;

        return localDate.atTime(time);
    }

    @Override
    public void setValue(LocalDateTime obj) {
        if (obj != null) {
            datePicker.setValue(obj.toLocalDate());
            timePicker.setValue(obj.toLocalTime());
        }
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
