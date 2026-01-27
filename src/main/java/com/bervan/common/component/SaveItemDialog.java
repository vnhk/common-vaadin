package com.bervan.common.component;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.model.PersistableData;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractPageView;
import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class SaveItemDialog<ID extends Serializable, T extends PersistableData<ID>> extends AbstractPageView {
    protected final ComponentHelper<ID, T> componentHelper;
    protected final BaseService<ID, T> service;
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");
    private final BervanViewConfig bervanViewConfig;
    private final Class<T> tClass;
    @Setter
    private Function<T, T> customizeSavingInSaveFormFunction = t -> t;
    @Setter
    private Function<T, T> customizePostSaveFunction = t -> t;

    public SaveItemDialog(ComponentHelper<ID, T> componentHelper, BaseService<ID, T> service, BervanViewConfig bervanViewConfig, Class<T> tClass) {
        this.componentHelper = componentHelper;
        this.bervanViewConfig = bervanViewConfig;
        this.service = service;
        this.tClass = tClass;
    }

    public void openSaveDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("80vw");
        dialog.add(buildSaveItemDialog(dialog));
        dialog.open();
    }

    private VerticalLayout buildSaveItemDialog(Dialog dialog) {
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);

        dialogLayout.getThemeList().remove("spacing");
        dialogLayout.getThemeList().remove("padding");

        headerLayout.getThemeList().remove("spacing");
        headerLayout.getThemeList().remove("padding");

        try {
            VerticalLayout formLayout = new VerticalLayout();

            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            Set<String> fieldNames = bervanViewConfig.getFieldNamesForSaveForm(tClass);
            List<Field> declaredFields = Arrays.stream(tClass.getDeclaredFields())
                    .filter(e -> fieldNames.contains(e.getName()))
                    .toList();

            // Build form fields based on VaadinBervanColumn annotations
            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(bervanViewConfig, field, null, false);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.getThemeList().remove("spacing");
                layoutForField.getThemeList().remove("padding");
                layoutForField.add((Component) componentWithValue);
                customFieldInFormItemLayout(field, layoutForField, componentWithValue);
                formLayout.add(layoutForField);
                fieldsHolder.put(field, componentWithValue);
                fieldsLayoutHolder.put(field, layoutForField);
            }

            customFieldInFormItemLayout(fieldsHolder, fieldsLayoutHolder, formLayout);

            // Create save button
            Button dialogSaveButton = new BervanButton("Save");

            // Create buttons layout
            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setWidthFull();
            buttonsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
            buttonsLayout.add(dialogSaveButton);

            T itemFinal = tClass.getConstructor().newInstance();
            // Handle save button click
            dialogSaveButton.addClickListener(buttonClickEvent -> {
                boolean isInvalid = false;
                for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                    fieldAutoConfigurableFieldEntry.getValue().validate();
                    if (!isInvalid) {
                        isInvalid = fieldAutoConfigurableFieldEntry.getValue().isInvalid();
                    }
                }

                if (isInvalid) {
                    showErrorNotification("Invalid value(s)");
                    return;
                }

                try {
                    // Update item fields with form values
                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(itemFinal, componentHelper.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    // Apply custom modifications before saving
                    T updatedItem = customizeSavingInSaveForm(itemFinal);

                    // Save the item
                    updatedItem = service.save(updatedItem); // save item
                    postSaveItemActions(updatedItem);

                    showSuccessNotification("Item saved successfully!");
                } catch (Exception e) {
                    log.error("Could not save item!", e);
                    showErrorNotification("Could not save item!");
                }
                dialog.close();
            });


            dialogLayout.add(headerLayout, formLayout, buttonsLayout);
        } catch (Exception e) {
            log.error("Error during using save modal. Check columns name or create custom modal!", e);
            showErrorNotification("Error during using save modal. Check columns name or create custom modal!");
        }

        return dialogLayout;
    }


    /**
     * Allows customization of the item before saving in save form.
     * Override this method to add custom logic before saving edited items
     * or set new customizeSavingInSaveFormFunction
     *
     * @param item The item to be saved
     * @return The modified item
     */
    protected T customizeSavingInSaveForm(T item) {
        item = customizeSavingInSaveFormFunction.apply(item);
        return item;
    }

    /**
     * Allows customization of individual fields in the save item layout.
     * Override this method to modify field appearance or behavior.
     *
     * @param field The field being processed
     * @param layoutForField The layout containing the field component
     * @param componentWithValue The field component
     */
    protected void customFieldInFormItemLayout(Field field, VerticalLayout layoutForField, AutoConfigurableField componentWithValue) {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Executes actions after successfully saving an item.
     * Override this method to add custom post-save logic.
     */
    protected void postSaveItemActions(T updatedItem) {
        customizePostSaveFunction.apply(updatedItem);
    }

    /**
     * Allows customization of the entire save form layout.
     * Override this method to modify the overall form structure.
     *
     * @param fieldsHolder Map of fields to their components
     * @param fieldsLayoutHolder Map of fields to their layout containers
     * @param formLayout The main form layout
     */
    protected void customFieldInFormItemLayout(Map<Field, AutoConfigurableField> fieldsHolder,
                                               Map<Field, VerticalLayout> fieldsLayoutHolder,
                                               VerticalLayout formLayout) {
        // Default implementation - can be overridden by subclasses
    }


}
