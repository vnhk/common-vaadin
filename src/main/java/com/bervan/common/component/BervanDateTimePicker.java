package com.bervan.common.component;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BervanDateTimePicker extends HorizontalLayout implements AutoConfigurableField<LocalDateTime> {
    private DatePicker datePicker = new DatePicker();
    private TimePicker timePicker = new TimePicker();
    private boolean isRequired = false;

    public BervanDateTimePicker(boolean isRequired) {
        this(true, true, isRequired);
    }

    public BervanDateTimePicker(boolean datePickerEnabled, boolean timePickerEnabled, boolean isRequired) {
        setDefaultVerticalComponentAlignment(Alignment.CENTER);
        this.isRequired = isRequired;

        if (datePickerEnabled) {
            add(datePicker);
        }

        if (timePickerEnabled) {
            add(timePicker);
        }

        initListener();
    }

    public BervanDateTimePicker(String label, boolean isRequired) {
        setDefaultVerticalComponentAlignment(Alignment.CENTER);
        this.isRequired = isRequired;

        datePicker = new DatePicker(label);
        add(datePicker, timePicker);
        initListener();
    }

    public void setInputWidth(String width) {
        datePicker.setWidth(width);
        timePicker.setWidth(width);
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

    public void setValue(LocalDate obj) {
        if (obj != null) {
            datePicker.setValue(obj);
        }
    }

    public void setValue(LocalTime obj) {
        if (obj != null) {
            timePicker.setValue(obj);
        }
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (datePicker != null) {
            datePicker.setReadOnly(readOnly);
        }
        if (timePicker != null) {
            timePicker.setReadOnly(readOnly);
        }
    }

    public void setLabel(String label) {
        datePicker.setLabel(label);
    }

    public void setNullValue() {
        datePicker.setValue(null);
        timePicker.setValue(null);
    }

    private void initListener() {
        datePicker.addValueChangeListener(event -> {
            validate();
        });
    }

    @Override
    public void validate() {
        if (isRequired && datePicker.isEmpty()) {
            datePicker.setInvalid(true);
        } else {
            datePicker.setInvalid(false);
        }
    }

    @Override
    public boolean isInvalid() {
        return datePicker.isInvalid();
    }
}
