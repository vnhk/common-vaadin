package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanIntegerField;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;

public class IntegerFieldBuilder implements ComponentForFieldBuilder {

    private static final IntegerFieldBuilder INSTANCE = new IntegerFieldBuilder();

    private IntegerFieldBuilder() {

    }

    public static IntegerFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildIntegerInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return (config.getIntValues() == null || config.getIntValues().isEmpty())
                && (Integer.class.getTypeName().equalsIgnoreCase(typeName) || typeName.equalsIgnoreCase("int"));
    }

    private AutoConfigurableField buildIntegerInput(Object value, String displayName) {
        BervanIntegerField field = new BervanIntegerField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue((Integer) value);
        return field;
    }
}
