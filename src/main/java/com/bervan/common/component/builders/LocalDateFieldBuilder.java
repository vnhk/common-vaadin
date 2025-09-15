package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanDatePicker;
import com.bervan.common.component.BervanTimePicker;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class LocalDateFieldBuilder implements ComponentForFieldBuilder {

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildLocalDateInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return CommonComponentUtils.hasTypMatch(config, LocalDate.class.getTypeName());
    }

    private AutoConfigurableField buildLocalDateInput(Object value, String displayName) {
        BervanDatePicker datePicker = new BervanDatePicker(displayName);
        datePicker.setLabel("Select date");

        if (value != null)
            datePicker.setValue((LocalDate) value);
        return datePicker;
    }
}
