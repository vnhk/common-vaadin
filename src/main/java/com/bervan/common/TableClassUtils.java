package com.bervan.common;

import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TableClassUtils {
    public static VaadinBervanColumnConfig buildColumnConfig(Field field) {
        VaadinBervanColumnConfig config = new VaadinBervanColumnConfig();
        config.setField(field);
        config.setExtension(field.getAnnotation(VaadinBervanColumn.class).extension());
        config.setInTable(field.getAnnotation(VaadinBervanColumn.class).inTable());
        config.setSortable(field.getAnnotation(VaadinBervanColumn.class).sortable());
        config.setTypeName(field.getType().getTypeName());
        config.setDisplayName(field.getAnnotation(VaadinBervanColumn.class).displayName());
        config.setInternalName(field.getAnnotation(VaadinBervanColumn.class).internalName());
        config.setWysiwyg(field.getAnnotation(VaadinBervanColumn.class).isWysiwyg());
        config.setDefaultValue(field.getAnnotation(VaadinBervanColumn.class).defaultValue());

        config.setStrValues(Arrays.stream(field.getAnnotation(VaadinBervanColumn.class).strValues()).toList());
        config.setIntValues(Arrays.stream(field.getAnnotation(VaadinBervanColumn.class).intValues()).boxed().collect(Collectors.toList()));

        return config;
    }

    public static Map<Field, Map<Object, Checkbox>> buildCheckboxFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, Map<Object, Checkbox>> filtersMap = new HashMap<>();
        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinBervanColumnConfig config = buildColumnConfig(field);
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
                    } else if (!config.getIntValues().isEmpty()) {
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

    public static Map<Field, Map<String, BervanDateTimePicker>> buildDateTimeFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, Map<String, BervanDateTimePicker>> filtersMap = new HashMap<>();
        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinBervanColumnConfig config = buildColumnConfig(field);
                if (field.getType().equals(LocalDateTime.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanDateTimePicker from = new BervanDateTimePicker();
                    BervanDateTimePicker to = new BervanDateTimePicker();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersMenuLayout.add(fieldLayout);
                } else if (field.getType().equals(LocalDate.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanDateTimePicker from = new BervanDateTimePicker(true, false);
                    BervanDateTimePicker to = new BervanDateTimePicker(true, false);
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersMenuLayout.add(fieldLayout);
                } else if (field.getType().equals(LocalTime.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanDateTimePicker from = new BervanDateTimePicker(false, true);
                    BervanDateTimePicker to = new BervanDateTimePicker(false, true);
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersMenuLayout.add(fieldLayout);
                }
            }
        }

        return filtersMap;
    }


    public static Map<Field, ? extends BervanTextField> buildTextFieldFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, BervanTextField> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinBervanColumnConfig config = buildColumnConfig(field);
                if (field.getType().equals(String.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanTextField bervanField = new BervanTextField();
                    fieldLayout.add(bervanField);
                    filtersMap.put(field, bervanField);

                    filtersMenuLayout.add(fieldLayout);
                }
            }
        }

        return filtersMap;
    }

    public static Map<Field, Map<String, BervanIntegerField>> buildIntegerFieldFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, Map<String, BervanIntegerField>> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinBervanColumnConfig config = buildColumnConfig(field);
                if (field.getType().equals(Integer.class) || field.getType().equals(Long.class) || field.getType().getName().equals("long")) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanIntegerField from = new BervanIntegerField();
                    BervanIntegerField to = new BervanIntegerField();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersMenuLayout.add(fieldLayout);
                }
            }
        }

        return filtersMap;
    }

    public static Map<Field, Map<String, BervanDoubleField>> buildDoubleFieldFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, Map<String, BervanDoubleField>> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinBervanColumnConfig config = buildColumnConfig(field);
                if (field.getType().equals(Double.class) || field.getType().equals(Float.class) || field.getType().getName().equals("double") || field.getType().getName().equals("float")) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanDoubleField from = new BervanDoubleField();
                    BervanDoubleField to = new BervanDoubleField();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersMenuLayout.add(fieldLayout);
                }
            }
        }

        return filtersMap;
    }

    public static Map<Field, Map<String, BervanBigDecimalField>> buildBigDecimalFieldFiltersMenu(List<Class<?>> classes, VerticalLayout filtersMenuLayout) {
        Map<Field, Map<String, BervanBigDecimalField>> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                VaadinBervanColumnConfig config = buildColumnConfig(field);
                if (field.getType().equals(BigDecimal.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    BervanBigDecimalField from = new BervanBigDecimalField();
                    BervanBigDecimalField to = new BervanBigDecimalField();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

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
                    .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                    .toList());
        }
        return res;
    }
}
