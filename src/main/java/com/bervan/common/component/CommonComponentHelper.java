package com.bervan.common.component;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableData;
import com.bervan.common.model.VaadinDynamicDropdownBervanColumn;
import com.bervan.common.model.VaadinDynamicMultiDropdownBervanColumn;
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

    public AutoConfigurableField buildComponentForField(BervanViewConfig bervanViewConfig, Field field, T item, boolean readOnly) throws IllegalAccessException {
        AutoConfigurableField component = null;
        ClassViewAutoConfigColumn config = buildColumnConfig(field, bervanViewConfig);

        field.setAccessible(true);
        Object value = item == null ? null : field.get(item);
        value = getInitValueForInput(bervanViewConfig, field, item, config, value);

        if (Objects.equals(config.getExtension(), VaadinDynamicDropdownBervanColumn.class.getSimpleName())) {
            String key = config.getInternalName();
            dynamicDropdownAllValues.put(key, getAllValuesForDynamicDropdowns(key, item));
            String initialSelectedValue = getInitialSelectedValueForDynamicDropdown(key, item);

            component = new BervanDynamicDropdownController(key, config.getDisplayName(), dynamicDropdownAllValues.get(key), initialSelectedValue);
        } else if (Objects.equals(config.getExtension(), VaadinDynamicMultiDropdownBervanColumn.class.getSimpleName())) {
            String key = config.getInternalName();
            dynamicMultiDropdownAllValues.put(key, getAllValuesForDynamicMultiDropdowns(key, item));
            List<String> initialSelectedValues = getInitialSelectedValueForDynamicMultiDropdown(key, item);

            component = new BervanDynamicMultiDropdownController(config.getInternalName(), config.getDisplayName(), dynamicMultiDropdownAllValues.get(key),
                    initialSelectedValues);
        } else if (!readOnly) {
            component = CommonComponentUtils.buildComponentForField(field, item, value, bervanViewConfig);
        } else {
            component = CommonComponentUtils.buildReadOnlyComponentForField(field, item, value, bervanViewConfig);
        }

        component.setId(config.getField() + "_id");

        field.setAccessible(false);

        return component;
    }

    protected boolean hasTypMatch(BervanViewConfig bervanViewConfig, ClassViewAutoConfigColumn config, String typeName) {
        Field field = Arrays.stream(tClass.getDeclaredFields()).filter(e -> e.getName().equals(config.getField())).findFirst().get();
        return field.getType().getTypeName().equals(typeName);
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

    protected Object getInitValueForInput(BervanViewConfig bervanViewConfig, Field field, Object item, ClassViewAutoConfigColumn config, Object value) throws IllegalAccessException {
        if (item == null) {
            if (!config.getDefaultValue().equals("")) {
                if (hasTypMatch(bervanViewConfig, config, String.class.getTypeName())) {
                    value = config.getDefaultValue();
                } else if (hasTypMatch(bervanViewConfig, config, Integer.class.getTypeName())) {
                    value = Integer.parseInt(String.valueOf(config.getDefaultValue()));
                } else if (hasTypMatch(bervanViewConfig, config, Double.class.getTypeName())) {
                    value = Double.parseDouble(String.valueOf(config.getDefaultValue()));
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
