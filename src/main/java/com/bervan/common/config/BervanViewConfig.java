package com.bervan.common.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BervanViewConfig extends HashMap<String, Map<String, ClassViewAutoConfigColumn>> {

    public Set<String> getFieldNames(Class<?> tClass) {
        return get(tClass.getSimpleName()).keySet();
    }

    public Set<String> getFieldNamesForSaveForm(Class<?> tClass) {
        Map<String, ClassViewAutoConfigColumn> stringClassViewAutoConfigColumnMap = get(tClass.getSimpleName());
        return stringClassViewAutoConfigColumnMap.entrySet().stream()
                .filter(e -> e.getValue().isInSaveForm())
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public Set<String> getEditableFieldNames(Class<?> tClass) {
        Map<String, ClassViewAutoConfigColumn> stringClassViewAutoConfigColumnMap = get(tClass.getSimpleName());
        return stringClassViewAutoConfigColumnMap.entrySet().stream()
                .filter(e -> e.getValue().isInEditForm())
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public boolean isAutoConfigurableField(Field field) {
        return getFieldNames(field.getDeclaringClass()).contains(field.getName());
    }

    public String getInternalName(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        for (ClassViewAutoConfigColumn config : get(declaringClass).values()) {
            if (!field.getName().equals(config.getField())) {
                continue;
            }

            if (config.getInternalName() == null || config.getInternalName().isEmpty()) {
                if (config.getInternalNameField() != null) {
                    String internalNameField = config.getInternalNameField();
                    try {
                        Field staticConstantField = declaringClass.getDeclaredField(internalNameField);
                        staticConstantField.setAccessible(true);
                        return (String) staticConstantField.get(null);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException("Cannot get internal name for " + field.getName(), e);
                    }
                } else {
                    throw new RuntimeException("Internal name is not defined for " + field.getName());
                }
            } else {
                return config.getInternalName();
            }
        }
        throw new RuntimeException("Internal name is not defined for " + field.getName());
    }

    public ClassViewAutoConfigColumn getFieldConfig(Field e) {
        return get(e.getDeclaringClass().getSimpleName()).get(e.getName());
    }
}
