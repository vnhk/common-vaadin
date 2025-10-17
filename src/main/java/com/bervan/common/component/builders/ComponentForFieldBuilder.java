package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;

public interface ComponentForFieldBuilder {
    AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config);

    default AutoConfigurableField buildReadOnlyField(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        AutoConfigurableField build = build(field, item, value, config);
        build.setReadOnly(true);
        return build;
    }

    boolean supports(String typeName, ClassViewAutoConfigColumn config);
}
