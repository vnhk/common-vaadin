package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanDateTimePicker;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class LocalDateTimeFieldBuilder implements ComponentForFieldBuilder {

    @Override
    public AutoConfigurableField<LocalDateTime> build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildLocalDateTimeInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return CommonComponentUtils.hasTypMatch(config, LocalDateTime.class.getTypeName());
    }

    private AutoConfigurableField<LocalDateTime> buildLocalDateTimeInput(Object value, String displayName) {
        BervanDateTimePicker dateTimePicker = new BervanDateTimePicker(displayName);
        dateTimePicker.setLabel("Select Date and Time");

        if (value != null)
            dateTimePicker.setValue((LocalDateTime) value);
        return dateTimePicker;
    }
}
