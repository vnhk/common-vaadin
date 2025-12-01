package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanJsonLogController;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.JsonLogViewerColumn;

import java.lang.reflect.Field;

public class JsonLogFieldBuilder implements ComponentForFieldBuilder {

    private static final JsonLogFieldBuilder INSTANCE = new JsonLogFieldBuilder();

    private JsonLogFieldBuilder() {
    }

    public static JsonLogFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildJsonLog(value, config);
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return JsonLogViewerColumn.class.getSimpleName().equals(config.getExtension());
    }

    private AutoConfigurableField<String> buildJsonLog(Object value, ClassViewAutoConfigColumn config) {
        if (value == null) return null;
        return new BervanJsonLogController(String.valueOf(value));
    }
}
