package com.bervan.common;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;

import java.time.LocalDateTime;

public class BervanDateTimePicker extends DateTimePicker implements AutoConfigurableField<LocalDateTime> {
    public BervanDateTimePicker() {
    }

    public BervanDateTimePicker(String label) {
        super(label);
    }

    public BervanDateTimePicker(String label, LocalDateTime initialDateTime) {
        super(label, initialDateTime);
    }

    public BervanDateTimePicker(LocalDateTime initialDateTime) {
        super(initialDateTime);
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }
}
