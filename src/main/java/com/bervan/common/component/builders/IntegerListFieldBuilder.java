package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.model.VaadinBervanColumnConfig;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

public class IntegerListFieldBuilder implements ComponentForFieldBuilder {

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        BervanComboBox comboBox = new BervanComboBox<>(config.getDisplayName());
        return buildComponentForComboBox(config.getIntValues(), comboBox, (Integer) value);
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
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
