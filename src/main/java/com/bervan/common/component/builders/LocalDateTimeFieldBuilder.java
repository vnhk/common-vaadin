package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanDateTimePicker;
import com.bervan.common.component.BervanTextArea;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;

import java.lang.reflect.Field;
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
    public AutoConfigurableField<String> build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildLocalDateTimeInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return CommonComponentUtils.hasTypMatch(config, LocalDateTime.class.getTypeName());
    }

    private AutoConfigurableField<String> buildLocalDateTimeInput(Object value, String displayName) {
        BervanDateTimePicker dateTimePicker = new BervanDateTimePicker(displayName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yy");

        if (value != null)
            dateTimePicker.setValue((LocalDateTime) value);
        return new BervanTextArea(displayName, dateTimePicker.getValue().format(formatter), "");
    }
}
