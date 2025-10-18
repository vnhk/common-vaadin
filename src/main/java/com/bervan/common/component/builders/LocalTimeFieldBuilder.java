package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanTimePicker;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;
import java.time.LocalTime;

public class LocalTimeFieldBuilder implements ComponentForFieldBuilder {

    private static final LocalTimeFieldBuilder INSTANCE = new LocalTimeFieldBuilder();

    private LocalTimeFieldBuilder() {
    }

    public static LocalTimeFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField<LocalTime> build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildLocalTimeInput(value, config);
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return LocalTime.class.getTypeName().equalsIgnoreCase(typeName);
    }

    private AutoConfigurableField<LocalTime> buildLocalTimeInput(Object value, ClassViewAutoConfigColumn config) {
        BervanTimePicker timePicker = new BervanTimePicker(config.getDisplayName(), config.isRequired());
        timePicker.setLabel("Select Time");

        if (value != null)
            timePicker.setValue((LocalTime) value);
        return timePicker;
    }
}
