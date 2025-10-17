package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanDoubleField;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;

public class DoubleFieldBuilder implements ComponentForFieldBuilder {
    private static final DoubleFieldBuilder INSTANCE = new DoubleFieldBuilder();

    private DoubleFieldBuilder() {
    }

    public static DoubleFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildDoubleInput(value, config.getDisplayName());
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return Double.class.getTypeName().equalsIgnoreCase(typeName) || typeName.equalsIgnoreCase("double");
    }

    private AutoConfigurableField buildDoubleInput(Object value, String displayName) {
        BervanDoubleField field = new BervanDoubleField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue(((Double) value));
        return field;
    }
}
