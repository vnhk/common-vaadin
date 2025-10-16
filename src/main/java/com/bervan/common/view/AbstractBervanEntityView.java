package com.bervan.common.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.CommonComponentHelper;
import com.bervan.common.component.ComponentHelper;
import com.bervan.common.model.PersistableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.service.BaseService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Hr;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
public abstract class AbstractBervanEntityView<ID extends Serializable, T extends PersistableData<ID>> extends AbstractPageView {
    protected final List<T> data = new LinkedList<>();
    protected final BaseService<ID, T> service;
    protected final VerticalLayout contentLayout = new VerticalLayout();
    protected final Class<T> tClass;
    protected T item;
    protected boolean buildDetails = true;
    protected MenuNavigationComponent pageLayout;
    protected HorizontalLayout topLayout = new HorizontalLayout();
    protected ComponentHelper<ID, T> componentHelper;
    protected final Button addButton = new BervanButton(new Icon(VaadinIcon.PLUS), e -> newItemButtonClick());
    protected final Button editButton = new BervanButton("✎", e -> openEditDialog());

    public AbstractBervanEntityView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, Class<T> tClass) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.tClass = tClass;
        this.componentHelper = new CommonComponentHelper<>(tClass);
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

    public void renderCommonComponents() {
        contentLayout.add(topLayout);
        Optional<VerticalLayout> verticalLayout = buildItemReadOnlyDetails();
        if (verticalLayout.isPresent()) {
            contentLayout.add(verticalLayout.get());
            contentLayout.add(new Hr());
        }
        contentLayout.add(new HorizontalLayout(addButton, editButton));
        if (pageLayout != null) {
            add(pageLayout);
        }
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
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, item, false);
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
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, item, true);
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

    protected Optional<VerticalLayout> buildItemReadOnlyDetails() {
        if (!buildDetails) {
            return Optional.empty();
        }

        VerticalLayout formLayout = new VerticalLayout();
        try {
            if (item == null) {
                showErrorNotification("Could not show item! No item is provided!");
                return Optional.empty();
            }

            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            List<Field> declaredFields = getVaadinTableFields().stream()
                    .toList();

            // Create two-column layout
            VerticalLayout leftColumn = new VerticalLayout();
            VerticalLayout rightColumn = new VerticalLayout();
            leftColumn.getThemeList().remove("spacing");
            leftColumn.getThemeList().remove("padding");
            rightColumn.getThemeList().remove("spacing");
            rightColumn.getThemeList().remove("padding");

            HorizontalLayout twoColumnLayout = new HorizontalLayout(leftColumn, rightColumn);
            twoColumnLayout.setWidthFull();
            twoColumnLayout.getThemeList().remove("spacing");
            twoColumnLayout.getThemeList().remove("padding");

            int fieldIndex = 0;
            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, item, true);
                componentWithValue.setReadOnly(true);
                componentWithValue.setWidthFull();

                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.getThemeList().remove("spacing");
                layoutForField.getThemeList().remove("padding");
                layoutForField.add((Component) componentWithValue);
                customFieldInEditItemLayout(field, layoutForField, componentWithValue);

                // Alternate between left and right column
                if (fieldIndex % 2 == 0) {
                    leftColumn.add(layoutForField);
                } else {
                    rightColumn.add(layoutForField);
                }

                fieldsHolder.put(field, componentWithValue);
                fieldsLayoutHolder.put(field, layoutForField);
                fieldIndex++;
            }

            formLayout.add(twoColumnLayout);
            customFieldInDetailsLayout(fieldsHolder, fieldsLayoutHolder, formLayout);

        } catch (Exception e) {
            log.error("Error during creating item details. Check columns name or create custom logic!", e);
            showErrorNotification("Error during creating item details.");
        }

        return Optional.of(formLayout);
    }

    protected void customFieldInDetailsLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {

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
                    .filter(e -> e.getAnnotation(VaadinBervanColumn.class).inEditForm())
                    .toList();

            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, item, false);
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
                        fieldAutoConfigurableFieldEntry.getKey().set(item, componentHelper.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
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
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, null, false);
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
                        fieldAutoConfigurableFieldEntry.getKey().set(newObject, componentHelper.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
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