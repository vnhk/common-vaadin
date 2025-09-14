package com.bervan.common.component;

import com.bervan.common.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        if (config.getExtension() == VaadinImageBervanColumn.class) {
            List<String> imageSources = new ArrayList<>();
            //
            if (hasTypMatch(config, String.class.getTypeName())) {
                imageSources.add((String) value);
                component = new BervanImageController(imageSources);
            } else if (hasTypMatch(config, List.class.getTypeName())) {
                if (value != null) {
                    imageSources.addAll((Collection<String>) value);
                }
                component = new BervanImageController(imageSources);
            }
        } else if (config.getExtension() == VaadinDynamicDropdownBervanColumn.class) {
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
        } else if (config.getStrValues().size() > 0) {
            BervanComboBox<String> comboBox = new BervanComboBox<>(config.getDisplayName());
            component = buildComponentForComboBox(config.getStrValues(), comboBox, ((String) value));
        } else if (config.getIntValues().size() > 0) {
            BervanComboBox<Integer> comboBox = new BervanComboBox<>(config.getDisplayName());
            component = buildComponentForComboBox(config.getIntValues(), comboBox, ((Integer) value));
        } else if (hasTypMatch(config, String.class.getTypeName())) {
            component = buildTextArea(value, config.getDisplayName(), config.isWysiwyg());
        } else if (hasTypMatch(config, Integer.class.getTypeName())) {
            component = buildIntegerInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, Long.class.getTypeName())) {
            component = buildLongInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, BigDecimal.class.getTypeName())) {
            component = buildBigDecimalInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, Double.class.getTypeName())) {
            component = buildDoubleInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, LocalTime.class.getTypeName())) {
            component = buildTimeInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, LocalDate.class.getTypeName())) {
            component = buildDateInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, LocalDateTime.class.getTypeName())) {
            component = buildDateTimeInput(value, config.getDisplayName());
        } else if (hasTypMatch(config, boolean.class.getTypeName())) {
            component = buildBooleanInput(value, config.getDisplayName());
        } else {
            component = new BervanTextField("Not supported yet");
            if (value == null) {
                component.setValue("");
            } else {
                component.setValue(value);
            }
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

    protected AutoConfigurableField<LocalDateTime> buildDateTimeInput(Object value, String displayName) {
        BervanDateTimePicker dateTimePicker = new BervanDateTimePicker(displayName);
        dateTimePicker.setLabel("Select Date and Time");

        if (value != null)
            dateTimePicker.setValue((LocalDateTime) value);
        return dateTimePicker;
    }

    protected AutoConfigurableField<LocalTime> buildTimeInput(Object value, String displayName) {
        BervanTimePicker timePicker = new BervanTimePicker(displayName);
        timePicker.setLabel("Select Time");

        if (value != null)
            timePicker.setValue((LocalTime) value);
        return timePicker;
    }

    protected AutoConfigurableField<LocalDate> buildDateInput(Object value, String displayName) {
        BervanDatePicker datePicker = new BervanDatePicker(displayName);
        datePicker.setLabel("Select date");

        if (value != null)
            datePicker.setValue((LocalDate) value);
        return datePicker;
    }

    protected AutoConfigurableField<Integer> buildIntegerInput(Object value, String displayName) {
        BervanIntegerField field = new BervanIntegerField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue((Integer) value);
        return field;
    }

    protected AutoConfigurableField<Double> buildLongInput(Object value, String displayName) {
        BervanLongField field = new BervanLongField(displayName);
        field.setWidthFull();
        if (value != null) {
            Long value1 = (Long) value;
            field.setValue(Double.valueOf(value1));
        }
        return field;
    }

    protected AutoConfigurableField<BigDecimal> buildBigDecimalInput(Object value, String displayName) {
        BervanBigDecimalField field = new BervanBigDecimalField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue((BigDecimal) value);
        return field;
    }

    protected AutoConfigurableField<Double> buildDoubleInput(Object value, String displayName) {
        BervanDoubleField field = new BervanDoubleField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue(((Double) value));
        return field;
    }

    protected AutoConfigurableField<String> buildTextArea(Object value, String displayName, boolean isWysiwyg) {
        AutoConfigurableField<String> textArea = new BervanTextArea(displayName);
        if (isWysiwyg) {
            textArea = new WysiwygTextArea("editor_" + UUID.randomUUID(), (String) value);
        }
        textArea.setWidthFull();
        if (value != null)
            textArea.setValue((String) value);
        return textArea;
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

    protected AutoConfigurableField buildBooleanInput(Object value, String displayName) {
        BervanBooleanField checkbox = new BervanBooleanField();
        if (value != null) {
            checkbox.setValue((Boolean) value);
        }
        return checkbox;
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

    protected <X> AutoConfigurableField buildComponentForComboBox(List<X> values, BervanComboBox<X> comboBox, X initVal) {
        AutoConfigurableField componentWithValue;
        comboBox.setItems(values);
        comboBox.setWidth("100%");
        comboBox.setValue(initVal);
        componentWithValue = comboBox;
        return componentWithValue;
    }

    public Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry) {
        return fieldAutoConfigurableFieldEntry.getValue().getValue();
    }
}
