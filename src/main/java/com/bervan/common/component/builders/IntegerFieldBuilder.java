package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanIntegerField;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

public class IntegerFieldBuilder implements ComponentForFieldBuilder {

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildIntegerInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return config.getIntValues().isEmpty() && CommonComponentUtils.hasTypMatch(config, Integer.class.getTypeName());
    }

    private AutoConfigurableField buildIntegerInput(Object value, String displayName) {
        BervanIntegerField field = new BervanIntegerField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue((Integer) value);
        return field;
    }
}
