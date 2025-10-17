package com.bervan.common;

import com.bervan.common.component.*;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.vaadin.flow.component.Component;
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

import static com.bervan.common.view.AbstractPageView.createSearchSection;

public class TableClassUtils {
    public static ClassViewAutoConfigColumn buildColumnConfig(Field field, BervanViewConfig bervanViewConfig) {
        return bervanViewConfig.getFieldConfig(field);
    }

//    public static Map<Field, Map<Object, Checkbox>> buildCheckboxFiltersMenu(List<Class<?>> classes, List<Component> filtersLayout) {
//        Map<Field, Map<Object, Checkbox>> filtersMap = new HashMap<>();
//        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes);
//
//        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
//            for (Field field : fields.getValue()) {
//                ClassViewAutoConfigColumn config = buildColumnConfig(field);
//                if (!config.getStrValues().isEmpty() || !config.getIntValues().isEmpty()) {
//                    VerticalLayout fieldLayout = new VerticalLayout();
//                    fieldLayout.setWidthFull();
//                    filtersMap.putIfAbsent(field, new HashMap<>());
//                    if (!config.getStrValues().isEmpty()) {
//                        for (String val : config.getStrValues()) {
//                            Checkbox checkbox = new Checkbox(val);
//                            checkbox.setValue(true);
//                            filtersMap.get(field).put(val, checkbox);
//                            fieldLayout.add(checkbox);
//                        }
//                    } else if (!config.getIntValues().isEmpty()) {
//                        for (Integer val : config.getIntValues()) {
//                            Checkbox checkbox = new Checkbox(val.toString());
//                            checkbox.setValue(true);
//                            filtersMap.get(field).put(val, checkbox);
//                            fieldLayout.add(checkbox);
//                        }
//                    }
//                    filtersLayout.add(createSearchSection(config.getDisplayName(), fieldLayout));
//                }
//            }
//
//        }
//
//        return filtersMap;
//    }

    public static Map<Field, Map<String, BervanDateTimePicker>> buildDateTimeFiltersMenu(List<Class<?>> classes, List<Component> filtersLayout, BervanViewConfig bervanViewConfig) {
        Map<Field, Map<String, BervanDateTimePicker>> filtersMap = new HashMap<>();
        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes, bervanViewConfig);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
                if (field.getType().equals(LocalDateTime.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    BervanDateTimePicker from = new BervanDateTimePicker();
                    BervanDateTimePicker to = new BervanDateTimePicker();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersLayout.add(createSearchSection(config.getDisplayName(), fieldLayout));
                } else if (field.getType().equals(LocalDate.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    BervanDateTimePicker from = new BervanDateTimePicker(true, false);
                    BervanDateTimePicker to = new BervanDateTimePicker(true, false);
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersLayout.add(createSearchSection(config.getDisplayName(), fieldLayout));
                } else if (field.getType().equals(LocalTime.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    BervanDateTimePicker from = new BervanDateTimePicker(false, true);
                    BervanDateTimePicker to = new BervanDateTimePicker(false, true);
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersLayout.add(createSearchSection(config.getDisplayName(), fieldLayout));
                }
            }
        }

        return filtersMap;
    }


    public static Map<Field, ? extends BervanTextField> buildTextFieldFiltersMenu(List<Class<?>> classes, List<Component> filtersLayout, BervanViewConfig bervanViewConfig) {
        Map<Field, BervanTextField> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes, bervanViewConfig);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
                if (field.getType().equals(String.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    BervanTextField bervanField = new BervanTextField();
                    fieldLayout.add(bervanField);
                    filtersMap.put(field, bervanField);

                    filtersLayout.add(createSearchSection(config.getDisplayName()
                            , fieldLayout));

                }
            }
        }

        return filtersMap;
    }

    public static Map<Field, Map<String, BervanIntegerField>> buildIntegerFieldFiltersMenu(List<Class<?>> classes, List<Component> filtersLayout, BervanViewConfig bervanViewConfig) {
        Map<Field, Map<String, BervanIntegerField>> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes, bervanViewConfig);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
                if (field.getType().equals(Integer.class) || field.getType().equals(Long.class) || field.getType().getName().equals("long")) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    BervanIntegerField from = new BervanIntegerField();
                    BervanIntegerField to = new BervanIntegerField();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersLayout.add(createSearchSection(config.getDisplayName()
                            , fieldLayout));

                }
            }
        }

        return filtersMap;
    }

    public static Map<Field, Map<String, BervanDoubleField>> buildDoubleFieldFiltersMenu(List<Class<?>> classes, List<Component> filtersLayout, BervanViewConfig bervanViewConfig) {
        Map<Field, Map<String, BervanDoubleField>> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes, bervanViewConfig);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
                if (field.getType().equals(Double.class) || field.getType().equals(Float.class) || field.getType().getName().equals("double") || field.getType().getName().equals("float")) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    BervanDoubleField from = new BervanDoubleField();
                    BervanDoubleField to = new BervanDoubleField();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersLayout.add(createSearchSection(config.getDisplayName()
                            , fieldLayout));

                }
            }
        }

        return filtersMap;
    }

    public static Map<Field, Map<String, BervanBigDecimalField>> buildBigDecimalFieldFiltersMenu(List<Class<?>> classes, List<Component> filtersLayout, BervanViewConfig bervanViewConfig) {
        Map<Field, Map<String, BervanBigDecimalField>> filtersMap = new HashMap<>();

        Map<Class<?>, List<Field>> classfields = getVaadinTableColumns(classes, bervanViewConfig);

        for (Map.Entry<Class<?>, List<Field>> fields : classfields.entrySet()) {
            for (Field field : fields.getValue()) {
                ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);
                if (field.getType().equals(BigDecimal.class)) {
                    VerticalLayout fieldLayout = new VerticalLayout();
                    fieldLayout.setWidthFull();
                    filtersMap.putIfAbsent(field, new HashMap<>());
                    BervanBigDecimalField from = new BervanBigDecimalField();
                    BervanBigDecimalField to = new BervanBigDecimalField();
                    fieldLayout.add(new HorizontalLayout(from, new H4(" -> "), to));
                    filtersMap.get(field).put("FROM", from);
                    filtersMap.get(field).put("TO", to);

                    filtersLayout.add(createSearchSection(config.getDisplayName()
                            , fieldLayout));

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

    public static Map<Class<?>, List<Field>> getVaadinTableColumns(List<Class<?>> classes, BervanViewConfig bervanViewConfig) {
        Map<Class<?>, List<Field>> res = new HashMap<>();
        for (Class<?> aClass : classes) {
            Set<String> fieldNames = bervanViewConfig.getFieldNames(aClass);
            res.put(aClass, Arrays.stream(aClass.getDeclaredFields())
                    .filter(e -> fieldNames.contains(e.getName()))
                    .toList());
        }
        return res;
    }
}
