package com.bervan.common.config;

import com.bervan.core.model.BaseModel;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EntityConfigValidator {

    private final BervanViewConfig viewConfig;

    public EntityConfigValidator(BervanViewConfig viewConfig) {
        this.viewConfig = viewConfig;
    }

    public <ID extends Serializable> List<FieldError> validateAll(String entityName, BaseModel<ID> model) {
        Map<String, Object> fields = new HashMap<>();
        Field[] declaredFields = model.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(model);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            fields.put(field.getName(), value);
        }
        return validate(entityName, fields);
    }

    public <ID extends Serializable> List<FieldError> validateNotNull(String entityName, BaseModel<ID> model) {
        Map<String, Object> fields = new HashMap<>();
        Field[] declaredFields = model.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(model);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (value == null) continue; // Skip null values for update it will not be updated
            fields.put(field.getName(), value);
        }
        return validate(entityName, fields);
    }

    public List<FieldError> validate(String entityName, Map<String, Object> fields) {
        List<FieldError> errors = new ArrayList<>();
        Map<String, ClassViewAutoConfigColumn> config = viewConfig.get(entityName);
        if (config == null) return errors;

        for (Map.Entry<String, ClassViewAutoConfigColumn> entry : config.entrySet()) {
            String fieldName = entry.getKey();
            ClassViewAutoConfigColumn col = entry.getValue();
            if (!fields.containsKey(fieldName)) continue;

            Object raw = fields.get(fieldName);
            String value = raw != null ? raw.toString() : null;

            if (col.isRequired() && (value == null || value.isBlank())) {
                errors.add(new FieldError(fieldName, col.getDisplayName() + " is required"));
                continue;
            }

            if (value != null && !value.isBlank()) {
                if (ColumnDataType.NUMBER.toString().equalsIgnoreCase(col.getDataType())) {
                    try {
                        double num = Double.parseDouble(value);
                        if (num < col.getMin() || num > col.getMax()) {
                            errors.add(new FieldError(fieldName,
                                    col.getDisplayName() + " must be a number between " + col.getMin() + " and " + col.getMax()));
                        }
                    } catch (NumberFormatException e) {
                        errors.add(new FieldError(fieldName,
                                col.getDisplayName() + " must be a valid number"));
                    }
                } else {
                    int len = value.length();
                    if (len < col.getMin()) {
                        errors.add(new FieldError(fieldName,
                                col.getDisplayName() + " must be at least " + col.getMin() + " characters"));
                    } else if (len > col.getMax()) {
                        errors.add(new FieldError(fieldName,
                                col.getDisplayName() + " must be at most " + col.getMax() + " characters"));
                    }
                }
            }
        }

        return errors;
    }

    public Map<String, ClassViewAutoConfigColumn> getEntityConfig(String entityName) {
        Map<String, ClassViewAutoConfigColumn> config = viewConfig.get(entityName);
        return config != null ? config : java.util.Collections.emptyMap();
    }

    public record FieldError(String field, String message) {
    }
}
