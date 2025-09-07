package com.bervan.common.view;

import com.bervan.common.*;
import com.bervan.common.component.*;
import com.bervan.common.model.*;
import com.bervan.common.service.BaseService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.ClipboardHelper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

@Slf4j
public abstract class AbstractBervanEntityView<ID extends Serializable, T extends PersistableData<ID>> extends AbstractPageView {
    protected final List<T> data = new LinkedList<>();
    protected final BaseService<ID, T> service;
    protected final VerticalLayout contentLayout = new VerticalLayout();
    protected final Class<T> tClass;
    protected final Map<String, List<String>> dynamicMultiDropdownAllValues = new HashMap<>();
    protected final Map<String, List<String>> dynamicDropdownAllValues = new HashMap<>();
    protected final Button addButton = new BervanButton(new Icon(VaadinIcon.PLUS), e -> newItemButtonClick());
    protected T item;
    protected final Button editButton = new BervanButton("✎", e -> openEditDialog());
    protected MenuNavigationComponent pageLayout;
    protected HorizontalLayout topLayout = new HorizontalLayout();
    protected int amountOfWysiwygEditors = 0;

    public AbstractBervanEntityView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, Class<T> tClass) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.tClass = tClass;
    }

    protected void openEditDialog() {
        if (item == null) {
            showErrorNotification("Cannot edit item - item was not selected!");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("60vw");
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);

        dialogLayout.getThemeList().remove("spacing");
        dialogLayout.getThemeList().remove("padding");

        headerLayout.getThemeList().remove("spacing");
        headerLayout.getThemeList().remove("padding");

        buildEditItemDialogContent(dialog, dialogLayout, headerLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    protected <X> AutoConfigurableField buildComponentForComboBox(List<X> values, BervanComboBox<X> comboBox, X initVal) {
        AutoConfigurableField componentWithValue;
        comboBox.setItems(values);
        comboBox.setWidth("100%");
        comboBox.setValue(initVal);
        componentWithValue = comboBox;
        return componentWithValue;
    }

    public void renderCommonComponents() {
        contentLayout.add(topLayout, addButton, editButton);
        add(pageLayout);
        add(contentLayout);
    }

    protected String getCopyValue(Field field, T item, String clickedColumn, AutoConfigurableField componentWithValue) {
        try {
            field.setAccessible(true);
            Object value = null;
            value = item == null ? null : field.get(item);
            field.setAccessible(false);
            if (value instanceof String) {
                return ((String) value);
            } else if (value instanceof Number) {
                return (value.toString());
            } else if (value instanceof LocalDateTime) {
                return (value.toString());
            } else if (value instanceof LocalDate) {
                return (value.toString());
            } else if (value instanceof LocalTime) {
                return (value.toString());
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    protected void buildOnColumnClickDialogContent(Dialog dialog, VerticalLayout dialogLayout,
                                                   HorizontalLayout headerLayout, String clickedField, T item) {
        Field field = null;
        try {
            List<Field> declaredFields = getVaadinTableFields();

            Optional<Field> fieldOptional = declaredFields.stream()
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).internalName().equals(clickedField))
                    .filter(e -> !e.getAnnotation(VaadinBervanColumn.class).inEditForm())
                    .findFirst();

            Optional<Field> editableField = declaredFields.stream()
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).internalName().equals(clickedField))
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inEditForm())
                    .findFirst();

            if (editableField.isPresent()) {
                field = editableField.get();
                AutoConfigurableField componentWithValue = buildComponentForField(field, item);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.add((Component) componentWithValue);
                customFieldInEditLayout(layoutForField, componentWithValue, clickedField, item);

                Button dialogSaveButton = new BervanButton("Save");

                Button deleteButton = new BervanButton("Delete Item");
                deleteButton.addClassName("option-button-warning");

                ClipboardHelper clipboardHelper = getClipboardHelper(field, item, clickedField, componentWithValue);

                HorizontalLayout buttonsLayout = new HorizontalLayout();
                buttonsLayout.setWidthFull();
                buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                buttonsLayout.add(new HorizontalLayout(dialogSaveButton, clipboardHelper), deleteButton);

                deleteButton.addClickListener(buttonClickEvent -> {
                    modalDeleteItem(dialog, item);
                });

                Field finalField = field;
                AutoConfigurableField finalComponentWithValue = componentWithValue;
                dialogSaveButton.addClickListener(buttonClickEvent -> {
                    try {
                        finalField.setAccessible(true);
                        finalField.set(item, finalComponentWithValue.getValue());
                        finalField.setAccessible(false);

                        T toBeSaved = customPreUpdate(clickedField, layoutForField, item, finalField, finalComponentWithValue);

                        T changed = service.save(toBeSaved);

                        customPostUpdate(changed);

                    } catch (IllegalAccessException e) {
                        log.error("Could not update field value!", e);
                        showErrorNotification("Could not update value!");
                    }
                    dialog.close();
                });

                dialogLayout.add(headerLayout, layoutForField, buttonsLayout);
            } else if (fieldOptional.isPresent()) {
                field = fieldOptional.get();
                AutoConfigurableField componentWithValue = buildComponentForField(field, item);
                componentWithValue.setReadOnly(true);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.add((Component) componentWithValue);
                customFieldInEditLayout(layoutForField, componentWithValue, clickedField, item);

                Button deleteButton = new BervanButton("Delete Item");
                deleteButton.addClassName("option-button-warning");

                ClipboardHelper clipboardHelper = getClipboardHelper(field, item, clickedField, componentWithValue);

                HorizontalLayout buttonsLayout = new HorizontalLayout();
                buttonsLayout.setWidthFull();
                buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                buttonsLayout.add(new HorizontalLayout(clipboardHelper), deleteButton);

                deleteButton.addClickListener(buttonClickEvent -> {
                    modalDeleteItem(dialog, item);
                });

                dialogLayout.add(headerLayout, layoutForField, buttonsLayout);

            }
        } catch (Exception e) {
            log.error("Error during using edit modal. Check columns name or create custom modal!", e);
            showErrorNotification("Error during using edit modal. Check columns name or create custom modal!");
        } finally {
            if (field != null) {
                field.setAccessible(false);
            }
        }
    }

    protected void customPostUpdate(T changed) {

    }

    protected void modalDeleteItem(Dialog dialog, T item) {
        service.delete(item);
        dialog.close();
        showSuccessNotification("Deleted successfully!");
    }

    protected AutoConfigurableField buildComponentForField(Field field, T item) throws IllegalAccessException {
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


    protected ClipboardHelper getClipboardHelper(Field field, T item, String clickedColumn, AutoConfigurableField componentWithValue) {
        String copyValue = getCopyValue(field, item, clickedColumn, componentWithValue);
        Button copyButton = new BervanButton(new Icon(VaadinIcon.COPY_O), e -> showPrimaryNotification("Value copied!"));
        ClipboardHelper clipboardHelper = new ClipboardHelper(copyValue, copyButton);

        if (copyValue == null) {
            clipboardHelper.setVisible(false);
        }
        return clipboardHelper;
    }

    protected T customPreUpdate(String clickedColumn, VerticalLayout layoutForField, T item, Field finalField, AutoConfigurableField finalComponentWithValue) {
        Optional<Field> editedField = getVaadinTableField(clickedColumn);
        if (editedField.isPresent()) {
            ID id = item.getId();
            T itemInDB = service.loadById(id).get();
            Field field = editedField.get();

            field.setAccessible(true);
            try {
                field.set(itemInDB, field.get(item));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            field.setAccessible(false);
            return itemInDB;
        }

        return item;
    }

    protected List<Field> getVaadinTableFields() {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .toList();
    }

    protected Optional<Field> getVaadinTableField(String clickedColumnKey) {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .filter(e -> e.getAnnotation(VaadinBervanColumn.class).internalName().equals(clickedColumnKey))
                .findFirst();
    }

    protected void customFieldInEditLayout(VerticalLayout layoutForField, AutoConfigurableField
            componentWithValue, String clickedColumn, T item) {

    }

    protected void customFieldInCreateItemLayout(Field field, VerticalLayout layoutForField, AutoConfigurableField componentWithValue) {

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
            amountOfWysiwygEditors++;
            textArea = new WysiwygTextArea("editor_" + amountOfWysiwygEditors, (String) value);
        }
        textArea.setWidthFull();
        if (value != null)
            textArea.setValue((String) value);
        return textArea;
    }

    protected Span formatTextComponent(String text) {
        if (text == null) {
            return new Span("");
        }
        Span span = new Span();
        span.getElement().setProperty("innerHTML", text.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"));
        return span;
    }

    protected void newItemButtonClick() {
        Dialog dialog = new Dialog();
        dialog.setWidth("60vw");
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);

        dialogLayout.getThemeList().remove("spacing");
        dialogLayout.getThemeList().remove("padding");

        headerLayout.getThemeList().remove("spacing");
        headerLayout.getThemeList().remove("padding");

        buildNewItemDialogContent(dialog, dialogLayout, headerLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    protected void buildEditItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout) {
        try {
            if (item == null) {
                showErrorNotification("Could not edit item! No item is selected!");
                return;
            }

            VerticalLayout formLayout = new VerticalLayout();

            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            List<Field> declaredFields = getVaadinTableFields().stream()
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inSaveForm())
                    .toList();

            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = buildComponentForField(field, item);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.getThemeList().remove("spacing");
                layoutForField.getThemeList().remove("padding");
                layoutForField.add((Component) componentWithValue);
                customFieldInEditItemLayout(field, layoutForField, componentWithValue);
                formLayout.add(layoutForField);
                fieldsHolder.put(field, componentWithValue);
                fieldsLayoutHolder.put(field, layoutForField);
            }

            customFieldInEditItemLayout(fieldsHolder, fieldsLayoutHolder, formLayout);

            Button dialogSaveButton = new BervanButton("Save");

            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setWidthFull();
            buttonsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

            buttonsLayout.add(dialogSaveButton);

            dialogSaveButton.addClickListener(buttonClickEvent -> {
                try {
                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(item, getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    item = customizeSavingInCreateForm(item);

                    service.save(item);

                    postEditItemActions();
                } catch (Exception e) {
                    log.error("Could not edit item!", e);
                    showErrorNotification("Could not edit item!");
                }
                dialog.close();
            });

            dialogLayout.add(headerLayout, formLayout, buttonsLayout);
        } catch (Exception e) {
            log.error("Error during using edit modal. Check columns name or create custom modal!", e);
            showErrorNotification("Error during using edit modal. Check columns name or create custom modal!");
        }
    }

    protected void buildNewItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout) {
        try {
            VerticalLayout formLayout = new VerticalLayout();

            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            List<Field> declaredFields = getVaadinTableFields().stream()
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inSaveForm())
                    .toList();

            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = buildComponentForField(field, null);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.getThemeList().remove("spacing");
                layoutForField.getThemeList().remove("padding");
                layoutForField.add((Component) componentWithValue);
                customFieldInCreateItemLayout(field, layoutForField, componentWithValue);
                formLayout.add(layoutForField);
                fieldsHolder.put(field, componentWithValue);
                fieldsLayoutHolder.put(field, layoutForField);
            }

            customFieldInCreateItemLayout(fieldsHolder, fieldsLayoutHolder, formLayout);

            Button dialogSaveButton = new BervanButton("Save");

            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setWidthFull();
            buttonsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

            buttonsLayout.add(dialogSaveButton);

            dialogSaveButton.addClickListener(buttonClickEvent -> {
                try {
                    T newObject = tClass.getConstructor().newInstance();
                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(newObject, getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    newObject = customizeSavingInCreateForm(newObject);

                    service.save(newObject);

                    postSaveActions();
                } catch (Exception e) {
                    log.error("Could not save new item!", e);
                    showErrorNotification("Could not save new item!");
                }
                dialog.close();
            });

            dialogLayout.add(headerLayout, formLayout, buttonsLayout);
        } catch (Exception e) {
            log.error("Error during using creation modal. Check columns name or create custom modal!", e);
            showErrorNotification("Error during using creation modal. Check columns name or create custom modal!");
        }
    }

    protected void postSaveActions() {

    }

    protected void postEditItemActions() {

    }

    protected Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry) {
        return fieldAutoConfigurableFieldEntry.getValue().getValue();
    }

    protected void customFieldInCreateItemLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {

    }

    protected void customFieldInEditItemLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {

    }

    protected void customFieldInEditItemLayout(Field field, VerticalLayout layoutForField, AutoConfigurableField componentWithValue) {

    }

    protected T customizeSavingInCreateForm(T newItem) {
        return newItem;
    }

    protected T customizeEditingInEditItemForm(T item) {
        return item;
    }
}