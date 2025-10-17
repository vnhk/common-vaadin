package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanTextField;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;

public class NotSupportedFieldBuilder implements ComponentForFieldBuilder {

    private static final NotSupportedFieldBuilder INSTANCE = new NotSupportedFieldBuilder();

    private NotSupportedFieldBuilder() {
    }

    public static NotSupportedFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField<String> build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildNotSupported((String) value);
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return true;
    }

    private AutoConfigurableField<String> buildNotSupported(String initVal) {
        BervanTextField component = new BervanTextField("Not supported yet");
        if (initVal == null) {
            component.setValue("");
        } else {
            component.setValue(initVal);
        }

        return component;
    }
}
