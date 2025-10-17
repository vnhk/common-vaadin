package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.config.ClassViewAutoConfigColumn;


import java.lang.reflect.Field;
import java.util.List;

public class IntegerListFieldBuilder implements ComponentForFieldBuilder {
    private static final IntegerListFieldBuilder INSTANCE = new IntegerListFieldBuilder();

    private IntegerListFieldBuilder() {
    }

    public static IntegerListFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        BervanComboBox comboBox = new BervanComboBox<>(config.getDisplayName());
        return buildComponentForComboBox(config.getIntValues(), comboBox, (Integer) value);
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return !config.getIntValues().isEmpty();
    }

    private AutoConfigurableField buildComponentForComboBox(List values, BervanComboBox comboBox, Integer initVal) {
        AutoConfigurableField componentWithValue;
        comboBox.setItems(values);
        comboBox.setWidth("100%");
        comboBox.setValue(initVal);
        componentWithValue = comboBox;
        return componentWithValue;
    }

}
