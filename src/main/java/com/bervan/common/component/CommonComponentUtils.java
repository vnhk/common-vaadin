package com.bervan.common.component;

import com.bervan.common.component.builders.*;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

@Slf4j
public class CommonComponentUtils {

    public static final List<ComponentForFieldBuilder> componentBuilders = Arrays.asList(
            new IntegerListFieldBuilder(),
            new IntegerFieldBuilder(),
            new StringListFieldBuilder(),
            new StringFieldBuilder(),
            new BigDecimalFieldBuilder(),
            new BooleanFieldBuilder(),
            new LocalDateFieldBuilder(),
            new LocalTimeFieldBuilder(),
            new LocalDateTimeFieldBuilder(),
            new DoubleFieldBuilder(),
            new ImageFieldBuilder(),
            new NotSupportedFieldBuilder()
    );

    private CommonComponentUtils() {

    }

    public static VerticalLayout buildFormLayout(Class<?> tClass, Object item) throws IllegalAccessException {
        VerticalLayout formLayout = new VerticalLayout();

        Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
        Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
        List<Field> declaredFields = getVaadinTableFields(tClass).stream()
                .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inSaveForm())
                .toList();


        for (Field field : declaredFields) {
            Object value = getInitValueForInput(field, item, buildColumnConfig(field), null);
            AutoConfigurableField componentWithValue = buildComponentForField(field, item, value);
            VerticalLayout layoutForField = new VerticalLayout();
            layoutForField.getThemeList().remove("spacing");
            layoutForField.getThemeList().remove("padding");
            layoutForField.add((Component) componentWithValue);
            formLayout.add(layoutForField);
            fieldsHolder.put(field, componentWithValue);
            fieldsLayoutHolder.put(field, layoutForField);
        }

        return formLayout;
    }

    public static AutoConfigurableField buildComponentForField(Field field, Object o, Object value) {
        for (ComponentForFieldBuilder componentBuilder : componentBuilders) {
            if (componentBuilder.supports(field.getType(), buildColumnConfig(field))) {
                return componentBuilder.build(field, o, value, buildColumnConfig(field));
            }
        }

        throw new RuntimeException("No component builder found for " + field.getType().getName());
    }


    public static List<Field> getVaadinTableFields(Class<?> tClass) {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .toList();
    }

    public static boolean hasTypMatch(VaadinBervanColumnConfig config, String typeName) {
        return typeName.toLowerCase().contains(config.getTypeName().toLowerCase());
    }


    public static Object getInitValueForInput(Field field, Object item, VaadinBervanColumnConfig config, Object value) throws IllegalAccessException {
        if (item == null) {
            if (!config.getDefaultValue().equals("")) {
                if (hasTypMatch(config, String.class.getTypeName())) {
                    value = config.getDefaultValue();
                } else if (hasTypMatch(config, Integer.class.getTypeName())) {
                    value = Integer.parseInt(config.getDefaultValue());
                } else if (hasTypMatch(config, Double.class.getTypeName())) {
                    value = Double.parseDouble(config.getDefaultValue());
                }
            }
        } else {
            value = field.get(item);
        }
        return value;
    }

    public static Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry) {
        return fieldAutoConfigurableFieldEntry.getValue().getValue();
    }
}
