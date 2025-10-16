package com.bervan.common.component;

import com.bervan.common.component.builders.*;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

@Slf4j
public class CommonComponentUtils {

    public static final List<ComponentForFieldBuilder> componentBuilders = new ArrayList<>(Arrays.asList(
            IntegerListFieldBuilder.getInstance(),
            IntegerFieldBuilder.getInstance(),
            StringListFieldBuilder.getInstance(),
            StringFieldBuilder.getInstance(),
            BigDecimalFieldBuilder.getInstance(),
            BooleanFieldBuilder.getInstance(),
            LocalDateFieldBuilder.getInstance(),
            LocalTimeFieldBuilder.getInstance(),
            LocalDateTimeFieldBuilder.getInstance(),
            DoubleFieldBuilder.getInstance(),
            ImageFieldBuilder.getInstance(),
            NotSupportedFieldBuilder.getInstance()
    ));

    private CommonComponentUtils() {

    }

    public static void addComponentBuilder(ComponentForFieldBuilder componentBuilder) {
        if (componentBuilder != null && !componentBuilders.contains(componentBuilder)) {
            componentBuilders.add(componentBuilders.size() - 1, componentBuilder); //default needs to be last
        }
    }

    public static VerticalLayout buildFormLayout(Class<?> tClass, Object item, Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder) throws IllegalAccessException {
        VerticalLayout formLayout = new VerticalLayout();

        List<Field> declaredFields = getVaadinTableFields(tClass).stream()
                .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inSaveForm())
                .toList();


        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object value = getInitValueForInput(field, item, buildColumnConfig(field), null);
            AutoConfigurableField componentWithValue = buildComponentForField(field, item, value);
            VerticalLayout layoutForField = new VerticalLayout();
            layoutForField.getThemeList().remove("spacing");
            layoutForField.getThemeList().remove("padding");
            layoutForField.add((Component) componentWithValue);
            formLayout.add(layoutForField);
            fieldsHolder.put(field, componentWithValue);
            fieldsLayoutHolder.put(field, layoutForField);
            field.setAccessible(false);
        }

        return formLayout;
    }

    public static AutoConfigurableField buildComponentForField(Field field, Object o, Object value) {
        for (ComponentForFieldBuilder componentBuilder : componentBuilders) {
            VaadinBervanColumnConfig config = buildColumnConfig(field);
            if (componentBuilder.supports(field.getType(), config)) {
                return componentBuilder.build(field, o, value, config);
            }
        }

        throw new RuntimeException("No component builder found for " + field.getType().getName());
    }

    public static AutoConfigurableField buildReadOnlyComponentForField(Field field, Object o, Object value) {
        for (ComponentForFieldBuilder componentBuilder : componentBuilders) {
            VaadinBervanColumnConfig config = buildColumnConfig(field);
            if (componentBuilder.supports(field.getType(), config)) {
                return componentBuilder.buildReadOnlyField(field, o, value, config);
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
