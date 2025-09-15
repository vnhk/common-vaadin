package com.bervan.common.component;

import com.bervan.common.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

@Slf4j
public class CommonComponentHelper<ID extends Serializable, T extends PersistableData<ID>> implements ComponentHelper<ID, T> {
    protected final Class<T> tClass;
    protected final Map<String, List<String>> dynamicMultiDropdownAllValues = new HashMap<>();
    protected final Map<String, List<String>> dynamicDropdownAllValues = new HashMap<>();

    public CommonComponentHelper(Class<T> tClass) {
        this.tClass = tClass;
    }

    public AutoConfigurableField buildComponentForField(Field field, T item) throws IllegalAccessException {
        AutoConfigurableField component = null;
        VaadinBervanColumnConfig config = buildColumnConfig(field);

        field.setAccessible(true);
        Object value = item == null ? null : field.get(item);
        value = getInitValueForInput(field, item, config, value);

        if (config.getExtension() == VaadinDynamicDropdownBervanColumn.class) {
            String key = config.getInternalName();
            dynamicDropdownAllValues.put(key, getAllValuesForDynamicDropdowns(key, item));
            String initialSelectedValue = getInitialSelectedValueForDynamicDropdown(key, item);

            component = new BervanDynamicDropdownController(key, config.getDisplayName(), dynamicDropdownAllValues.get(key), initialSelectedValue);
        } else if (config.getExtension() == VaadinDynamicMultiDropdownBervanColumn.class) {
            String key = config.getInternalName();
            dynamicMultiDropdownAllValues.put(key, getAllValuesForDynamicMultiDropdowns(key, item));
            List<String> initialSelectedValues = getInitialSelectedValueForDynamicMultiDropdown(key, item);

            component = new BervanDynamicMultiDropdownController(config.getInternalName(), config.getDisplayName(), dynamicMultiDropdownAllValues.get(key),
                    initialSelectedValues);
        } else {
            component = CommonComponentUtils.buildComponentForField(field, item, value);
        }

        component.setId(config.getTypeName() + "_id");

        field.setAccessible(false);

        return component;
    }

    @Override
    public List<Field> getVaadinTableFields() {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .toList();
    }

    protected boolean hasTypMatch(VaadinBervanColumnConfig config, String typeName) {
        return typeName.toLowerCase().contains(config.getTypeName().toLowerCase());
    }

    protected List<String> getInitialSelectedValueForDynamicMultiDropdown(String key, T item) {
        log.warn("getInitialSelectedValueForDynamicMultiDropdown has been not overridden");
        return new ArrayList<>();
    }

    protected List<String> getAllValuesForDynamicMultiDropdowns(String key, T item) {
        log.warn("getAllValuesForDynamicMultiDropdowns has been not overridden");
        return new ArrayList<>();
    }

    protected String getInitialSelectedValueForDynamicDropdown(String key, T item) {
        log.warn("getInitialSelectedValueForDynamicDropdown has been not overridden");
        return null;
    }

    protected List<String> getAllValuesForDynamicDropdowns(String key, T item) {
        log.warn("getAllValuesForDynamicDropdowns has been not overridden");
        return new ArrayList<>();
    }

    protected Object getInitValueForInput(Field field, Object item, VaadinBervanColumnConfig config, Object value) throws IllegalAccessException {
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

    public Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry) {
        return fieldAutoConfigurableFieldEntry.getValue().getValue();
    }
}
