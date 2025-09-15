package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.model.VaadinBervanColumnConfig;

import java.lang.reflect.Field;

public interface ComponentForFieldBuilder {
    AutoConfigurableField build(Field field, Object item, Object value, VaadinBervanColumnConfig config);

    boolean supports(Class<?> extension, VaadinBervanColumnConfig config);
}
