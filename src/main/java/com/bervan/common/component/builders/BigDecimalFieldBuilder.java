package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanBigDecimalField;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class BigDecimalFieldBuilder implements ComponentForFieldBuilder {

    private static final BigDecimalFieldBuilder INSTANCE = new BigDecimalFieldBuilder();

    private BigDecimalFieldBuilder() {
    }

    public static BigDecimalFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildBigDecimalInput(value, config);
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return BigDecimal.class.getTypeName().equalsIgnoreCase(typeName);
    }

    private AutoConfigurableField<BigDecimal> buildBigDecimalInput(Object value, ClassViewAutoConfigColumn config) {
        BervanBigDecimalField field = new BervanBigDecimalField(config.getDisplayName(), config.isRequired());
        field.setWidthFull();
        if (value != null)
            field.setValue((BigDecimal) value);
        return field;
    }
}
