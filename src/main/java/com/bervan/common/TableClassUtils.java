package com.bervan.common;

import com.bervan.common.model.VaadinTableColumn;
import com.bervan.common.model.VaadinTableColumnConfig;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class TableClassUtils {
    public static VaadinTableColumnConfig buildColumnConfig(Field field) {
        VaadinTableColumnConfig config = new VaadinTableColumnConfig();
        config.setField(field);
        config.setTypeName(field.getType().getTypeName());
        config.setDisplayName(field.getAnnotation(VaadinTableColumn.class).displayName());
        config.setInternalName(field.getAnnotation(VaadinTableColumn.class).internalName());
        config.setWysiwyg(field.getAnnotation(VaadinTableColumn.class).isWysiwyg());
        config.setDefaultValue(field.getAnnotation(VaadinTableColumn.class).defaultValue());

        config.setStrValues(Arrays.stream(field.getAnnotation(VaadinTableColumn.class).strValues()).toList());
        config.setIntValues(Arrays.stream(field.getAnnotation(VaadinTableColumn.class).intValues()).boxed().collect(Collectors.toList()));

        return config;
    }


    public static Map<Field, Map<Object, Checkbox>> buildFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, Map<Object, Checkbox>> filtersMap = new HashMap<>();
        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinTableColumnConfig config = buildColumnConfig(field);
                if (!config.getStrValues().isEmpty() || !config.getIntValues().isEmpty()) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    if (!config.getStrValues().isEmpty()) {
                        H4 label = new H4(config.getDisplayName() + ":");
                        fieldLayout.add(label);
                        for (String val : config.getStrValues()) {
                            Checkbox checkbox = new Checkbox(val);
                            checkbox.setValue(true);
                            filtersMap.get(field).put(val, checkbox);
                            fieldLayout.add(checkbox);
                        }
                    } else {
                        H4 label = new H4(config.getDisplayName() + ":");
                        fieldLayout.add(label);
                        for (Integer val : config.getIntValues()) {
                            Checkbox checkbox = new Checkbox(val.toString());
                            checkbox.setValue(true);
                            filtersMap.get(field).put(val, checkbox);
                            fieldLayout.add(checkbox);
                        }
                    }
                    filtersMenuLayout.add(fieldLayout);
                }
            }

        }

        return filtersMap;
    }

    public static Set<Object> getSelectedObjects(Map<Field, Map<Object, Checkbox>> filtersMap, Field field) {
        Map<Object, Checkbox> objectCheckboxMap = filtersMap.get(field);
        if (objectCheckboxMap == null) {
            return Set.of();
        }

        return objectCheckboxMap.entrySet().stream()
                .filter(entry -> entry.getValue().getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static Map<Class<?>, List<Field>> getVaadinTableColumns(List<Class<?>> classes) {
        Map<Class<?>, List<Field>> res = new HashMap<>();
        for (Class<?> aClass : classes) {
            res.put(aClass, Arrays.stream(aClass.getDeclaredFields())
                    .filter(e -> e.isAnnotationPresent(VaadinTableColumn.class))
                    .toList());
        }
        return res;
    }

}
