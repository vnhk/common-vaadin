package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanTimePicker;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;

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
    public AutoConfigurableField<LocalTime> build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildLocalTimeInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return CommonComponentUtils.hasTypMatch(config, LocalTime.class.getTypeName());
    }

    private AutoConfigurableField<LocalTime> buildLocalTimeInput(Object value, String displayName) {
        BervanTimePicker timePicker = new BervanTimePicker(displayName);
        timePicker.setLabel("Select Time");

        if (value != null)
            timePicker.setValue((LocalTime) value);
        return timePicker;
    }
}
