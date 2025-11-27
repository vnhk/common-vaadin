package com.bervan.common.component;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.model.PersistableData;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractPageView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class EditItemDialog<ID extends Serializable, T extends PersistableData<ID>> extends AbstractPageView {
    protected final ComponentHelper<ID, T> componentHelper;
    protected final BaseService<ID, T> service;
    private final BervanViewConfig bervanViewConfig;
    @Setter
    private Function<T, T> customizeSavingInEditFormFunction = t -> t;

    public EditItemDialog(ComponentHelper<ID, T> componentHelper, BaseService<ID, T> service, BervanViewConfig bervanViewConfig) {
        this.componentHelper = componentHelper;
        this.bervanViewConfig = bervanViewConfig;
        this.service = service;
    }

    public void openEditDialog(T item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("80vw");
        dialog.add(buildEditItemDialog(dialog, item));
        dialog.open();
    }

    public VerticalLayout buildEditItemDialog(Dialog dialog, T item) {
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
            Set<String> fieldNames = bervanViewConfig.getEditableFieldNames(item.getClass());
            List<Field> declaredFields = Arrays.stream(item.getClass().getDeclaredFields())
                    .filter(e -> fieldNames.contains(e.getName()))
                    .toList();

            // Build form fields based on VaadinBervanColumn annotations
            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(bervanViewConfig, field, item, false);
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
                    T updatedItem = customizeSavingInEditForm(itemFinal);

                    // Save the item
                    T existing = service.findById(updatedItem.getId()); // take item from db
                    BeanUtils.copyProperties(updatedItem, existing, "id", "historyEntities"); // copy updated values to existing item
                    updatedItem = service.save(existing); // save existing (updated) item
                    //todo fix for DTOs it also doesn't work in prod, its not related to the new auto config
                    //new DTOMapper(bervan new ArrayList<>()).map(updatedItem); should be used
                    //new dto implementation should be created

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

        return dialogLayout;
    }


    /**
     * Allows customization of the item before saving in edit form.
     * Override this method to add custom logic before saving edited items
     * or set new customizeSavingInEditFormFunction
     *
     * @param item The item to be saved
     * @return The modified item
     */
    protected T customizeSavingInEditForm(T item) {
        item = customizeSavingInEditFormFunction.apply(item);
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
