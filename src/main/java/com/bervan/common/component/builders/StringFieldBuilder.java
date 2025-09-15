package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanTextArea;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.component.WysiwygTextArea;
import com.bervan.common.model.VaadinBervanColumnConfig;

import java.lang.reflect.Field;
import java.util.UUID;

public class StringFieldBuilder implements ComponentForFieldBuilder {

    @Override
    public AutoConfigurableField<String> build(Field field, Object item, Object value, VaadinBervanColumnConfig config) {
        return buildTextArea(value, config.getDisplayName(), config.isWysiwyg());
    }

    @Override
    public boolean supports(Class<?> extension, VaadinBervanColumnConfig config) {
        return config.getStrValues().isEmpty() && CommonComponentUtils.hasTypMatch(config, String.class.getTypeName());
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
