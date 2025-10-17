package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanDateTimePicker;
import com.bervan.common.component.BervanTextArea;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeFieldBuilder implements ComponentForFieldBuilder {

    private static final LocalDateTimeFieldBuilder INSTANCE = new LocalDateTimeFieldBuilder();

    private LocalDateTimeFieldBuilder() {
    }

    public static LocalDateTimeFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField<LocalDateTime> build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildLocalDateTimeInput(value, config.getDisplayName());
    }

    @Override
    public AutoConfigurableField buildReadOnlyField(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        BervanDateTimePicker dateTimePicker = new BervanDateTimePicker(config.getDisplayName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yy");

        BervanTextArea readOnlyField = new BervanTextArea(config.getDisplayName());
        readOnlyField.setReadOnly(true);

        if (value == null) {
            return readOnlyField;
        }
        dateTimePicker.setValue((LocalDateTime) value);
        readOnlyField.setValue(dateTimePicker.getValue().format(formatter));
        return readOnlyField;
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return LocalDateTime.class.getTypeName().equals(typeName);
    }

    private AutoConfigurableField<LocalDateTime> buildLocalDateTimeInput(Object value, String displayName) {
        BervanDateTimePicker dateTimePicker = new BervanDateTimePicker(displayName);

        if (value == null) {
            return dateTimePicker;
        }

        dateTimePicker.setValue((LocalDateTime) value);
        return dateTimePicker;
    }
}
