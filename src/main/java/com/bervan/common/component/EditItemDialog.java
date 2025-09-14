package com.bervan.common.component;

import com.bervan.common.model.PersistableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractPageView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EditItemDialog<ID extends Serializable, T extends PersistableData<ID>> extends AbstractPageView {
    protected final ComponentHelper<ID, T> componentHelper;
    protected final BaseService<ID, T> service;

    public EditItemDialog(ComponentHelper<ID, T> componentHelper, BaseService<ID, T> service) {
        this.componentHelper = componentHelper;
        this.service = service;
    }

    protected Dialog buildEditItemDialog(T item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("60vw");
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);

        dialogLayout.getThemeList().remove("spacing");
        dialogLayout.getThemeList().remove("padding");

        headerLayout.getThemeList().remove("spacing");
        headerLayout.getThemeList().remove("padding");

        try {
            if (item == null) {
                showErrorNotification("Could not edit item! No item is selected!");
                return null;
            }

            VerticalLayout formLayout = new VerticalLayout();

            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            List<Field> declaredFields = componentHelper.getVaadinTableFields().stream()
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inEditForm())
                    .toList();

            // Build form fields based on VaadinBervanColumn annotations
            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, item);
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

            // Create save button
            Button dialogSaveButton = new BervanButton("Save");

            // Create buttons layout
            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setWidthFull();
            buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            buttonsLayout.add(dialogSaveButton);

            T itemFinal = item;
            // Handle save button click
            dialogSaveButton.addClickListener(buttonClickEvent -> {
                try {
                    // Update item fields with form values
                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(itemFinal, componentHelper.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    // Apply custom modifications before saving
                    T updatedItem = customizeSavingInEditForm(itemFinal);

                    // Save the item
                    updatedItem = service.save(updatedItem);

                    // Execute post-save actions
                    postEditItemActions(updatedItem);

                    showSuccessNotification("Item updated successfully!");
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

        return dialog;
    }


    /**
     * Allows customization of the item before saving in edit form.
     * Override this method to add custom logic before saving edited items.
     *
     * @param item The item to be saved
     * @return The modified item
     */
    protected T customizeSavingInEditForm(T item) {
        return item;
    }

    /**
     * Allows customization of individual fields in the edit item layout.
     * Override this method to modify field appearance or behavior.
     *
     * @param field The field being processed
     * @param layoutForField The layout containing the field component
     * @param componentWithValue The field component
     */
    protected void customFieldInEditItemLayout(Field field, VerticalLayout layoutForField, AutoConfigurableField componentWithValue) {
        // Default implementation - can be overridden by subclasses
    }

    /**
     * Executes actions after successfully editing an item.
     * Override this method to add custom post-edit logic.
     */
    protected void postEditItemActions(T updatedItem) {

    }

    /**
     * Allows customization of the entire edit form layout.
     * Override this method to modify the overall form structure.
     *
     * @param fieldsHolder Map of fields to their components
     * @param fieldsLayoutHolder Map of fields to their layout containers
     * @param formLayout The main form layout
     */
    protected void customFieldInEditItemLayout(Map<Field, AutoConfigurableField> fieldsHolder,
                                               Map<Field, VerticalLayout> fieldsLayoutHolder,
                                               VerticalLayout formLayout) {
        // Default implementation - can be overridden by subclasses
    }


}
