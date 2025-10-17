package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanDatePicker;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class LocalDateFieldBuilder implements ComponentForFieldBuilder {

    private static final LocalDateFieldBuilder INSTANCE = new LocalDateFieldBuilder();

    private LocalDateFieldBuilder() {
    }

    public static LocalDateFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildLocalDateInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return LocalDate.class.getTypeName().equals(typeName);
    }

    private AutoConfigurableField buildLocalDateInput(Object value, String displayName) {
        BervanDatePicker datePicker = new BervanDatePicker(displayName);
        datePicker.setLabel("Select date");

        if (value != null)
            datePicker.setValue((LocalDate) value);
        return datePicker;
    }
}
