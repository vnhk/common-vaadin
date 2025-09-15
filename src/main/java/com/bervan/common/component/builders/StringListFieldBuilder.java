package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.model.VaadinBervanColumnConfig;

import java.lang.reflect.Field;
import java.util.List;

public class StringListFieldBuilder implements ComponentForFieldBuilder {

    private static final StringListFieldBuilder INSTANCE = new StringListFieldBuilder();

    private StringListFieldBuilder() {
    }

    public static StringListFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField<String> build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        BervanComboBox comboBox = new BervanComboBox<>(config.getDisplayName());
        return buildComponentForComboBox(config.getStrValues(), comboBox, (String) value);
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return !config.getStrValues().isEmpty();
    }

    private AutoConfigurableField buildComponentForComboBox(List values, BervanComboBox comboBox, String initVal) {
        AutoConfigurableField componentWithValue;
        comboBox.setItems(values);
        comboBox.setWidth("100%");
        comboBox.setValue(initVal);
        componentWithValue = comboBox;
        return componentWithValue;
    }
}
