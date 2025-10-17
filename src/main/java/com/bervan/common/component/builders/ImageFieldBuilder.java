package com.bervan.common.component.builders;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanImageController;
import com.bervan.common.component.CommonComponentUtils;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.VaadinImageBervanColumn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ImageFieldBuilder implements ComponentForFieldBuilder {

    private static final ImageFieldBuilder INSTANCE = new ImageFieldBuilder();

    private ImageFieldBuilder() {
    }

    public static ImageFieldBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public AutoConfigurableField build(Field field, Object item, Object value, ClassViewAutoConfigColumn config) {
        return buildImageField(value, config);
    }

    @Override
    public boolean supports(String typeName, ClassViewAutoConfigColumn config) {
        return VaadinImageBervanColumn.class.getSimpleName().equals(config.getExtension());
    }

    private AutoConfigurableField<List<String>> buildImageField(Object value, ClassViewAutoConfigColumn config) {
        BervanImageController component = null;
        List<String> imageSources = new ArrayList<>();
        //
        if (CommonComponentUtils.hasTypMatch(value.getClass(), config, String.class.getTypeName())) {
            imageSources.add((String) value);
            component = new BervanImageController(imageSources);
        } else if (CommonComponentUtils.hasTypMatch(value.getClass(), config, List.class.getTypeName())
                || CommonComponentUtils.hasTypMatch(value.getClass(), config, LinkedList.class.getTypeName()) ||
                CommonComponentUtils.hasTypMatch(value.getClass(), config, ArrayList.class.getTypeName())) {
            if (value != null) {
                imageSources.addAll((Collection<String>) value);
            }
            component = new BervanImageController(imageSources);
        }

        return component;
    }
}
