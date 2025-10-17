package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanTextArea;
import com.bervan.common.component.WysiwygTextArea;
import com.bervan.common.config.ClassViewAutoConfigColumn;

import java.lang.reflect.Field;
import java.util.UUID;

public class StringFieldBuilder implements ComponentForFieldBuilder {

    private static final StringFieldBuilder INSTANCE = new StringFieldBuilder();

    private StringFieldBuilder() {
    }

    public static StringFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField<String> build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildTextArea(value, config.getDisplayName(), config.isWysiwyg());
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return config.getStrValues().isEmpty() && String.class.getTypeName().equals(typeName);
    }

    private AutoConfigurableField<String> buildTextArea(Object value, String displayName, boolean isWysiwyg) {
        AutoConfigurableField<String> textArea = new BervanTextArea(displayName);
        if (isWysiwyg) {
            textArea = new WysiwygTextArea("editor_" + UUID.randomUUID(), (String) value);
        }
        textArea.setWidthFull();
        if (value != null)
            textArea.setValue((String) value);
        return textArea;
    }
}
