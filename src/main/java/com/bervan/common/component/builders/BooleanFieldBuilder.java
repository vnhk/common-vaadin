package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanBooleanField;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;

import java.lang.reflect.Field;

public class BooleanFieldBuilder implements ComponentForFieldBuilder {

    private static final BooleanFieldBuilder INSTANCE = new BooleanFieldBuilder();

    private BooleanFieldBuilder() {
    }

    public static BooleanFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildBooleanInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return CommonComponentUtils.hasTypMatch(config, Boolean.class.getTypeName());
    }

    private AutoConfigurableField buildBooleanInput(Object value, String displayName) {
        BervanBooleanField checkbox = new BervanBooleanField();
        if (value != null) {
            checkbox.setValue((Boolean) value);
        }
        return checkbox;
    }
}
