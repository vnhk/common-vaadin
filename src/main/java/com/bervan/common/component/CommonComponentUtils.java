package com.bervan.common.component;

import com.bervan.common.component.builders.*;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

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

    public static VerticalLayout buildFormLayout(Class<?> tClass, Object item, Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, BervanViewConfig bervanViewConfig) throws IllegalAccessException {
        VerticalLayout formLayout = new VerticalLayout();

        Set<String> fieldNamesForSaveForm = bervanViewConfig.getFieldNamesForSaveForm(tClass);

        List<Field> declaredFields = Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> fieldNamesForSaveForm.contains(e.getName()))
                .toList();

        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object value = getInitValueForInput(field, item, buildColumnConfig(field, bervanViewConfig), null);
            AutoConfigurableField componentWithValue = buildComponentForField(field, item, value, bervanViewConfig);
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

    public static AutoConfigurableField buildComponentForField(Field field, Object o, Object value, BervanViewConfig bervanViewConfig) {
        for (ComponentForFieldBuilder componentBuilder : componentBuilders) {
            ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
            if (componentBuilder.supports(field.getType().getTypeName(), config)) {
                return componentBuilder.build(field, o, value, config);
            }
        }

        throw new RuntimeException("No component builder found for " + field.getType().getName());
    }

    public static AutoConfigurableField buildReadOnlyComponentForField(Field field, Object o, Object value, BervanViewConfig bervanViewConfig) {
        for (ComponentForFieldBuilder componentBuilder : componentBuilders) {
            ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
            if (componentBuilder.supports(field.getType().getTypeName(), config)) {
                return componentBuilder.buildReadOnlyField(field, o, value, config);
            }
        }

        throw new RuntimeException("No component builder found for " + field.getType().getName());
    }

    public static boolean hasTypMatch(Class<?> tClass, ClassViewAutoConfigColumn config, String typeName) {
        Field field = Arrays.stream(tClass.getDeclaredFields()).filter(e -> e.getName().equals(config.getField())).findFirst().get();
        return field.getType().getTypeName().equals(typeName);
    }


    public static Object getInitValueForInput(Field field, Object item, ClassViewAutoConfigColumn config, Object value) throws IllegalAccessException {
        if (item == null) {
            if (!config.getDefaultValue().equals("")) {
                if (hasTypMatch(field.getDeclaringClass(), config, String.class.getTypeName())) {
                    value = config.getDefaultValue();
                } else if (hasTypMatch(field.getDeclaringClass(), config, Integer.class.getTypeName())) {
                    value = Integer.parseInt(String.valueOf(config.getDefaultValue()));
                } else if (hasTypMatch(field.getDeclaringClass(), config, Double.class.getTypeName())) {
                    value = Double.parseDouble(String.valueOf(config.getDefaultValue()));
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
