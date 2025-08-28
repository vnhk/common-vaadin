package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BaseDTO;
import com.bervan.core.model.BaseModel;
import com.bervan.core.model.BervanLogger;
import com.bervan.core.service.DTOMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.olli.ClipboardHelper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public abstract class AbstractBervanTableDTOView<ID extends Serializable, T extends PersistableTableData<ID>, DTO extends BaseDTO<ID>> extends AbstractBervanTableView<ID, T> {
    private BervanLogger bervanLogger;
    private Class<DTO> dtoClass;

    public AbstractBervanTableDTOView(MenuNavigationComponent pageLayout, BaseService<ID, T> service, BervanLogger log, Class<T> tClass, Class<DTO> dtoClass) {
        super(pageLayout, service, log, tClass);
        this.bervanLogger = log;
        this.dtoClass = dtoClass;
    }

    @Override
    protected void refreshData() {
        try {
            this.data.removeAll(this.data);
            List<T> loadedData = loadData();
            List<T> converted = new ArrayList<>();
            DTOMapper dtoMapper = new DTOMapper(bervanLogger, new ArrayList<>());
            for (T d : loadedData) {
                converted.add((T) dtoMapper.map(((BaseModel) d), dtoClass));
            }
            this.data.addAll(converted);
            this.grid.getDataProvider().refreshAll();
        } catch (Exception e) {
            log.error("Failed to load data", e);
            showErrorNotification("Failed to load data");
        }
    }

    @Override
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

            DTOMapper dtoMapper = new DTOMapper(bervanLogger, new ArrayList<>());
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
                buttonsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

                buttonsLayout.add(new HorizontalLayout(dialogSaveButton, clipboardHelper), deleteButton);

                T finalItem = service.loadById(item.getId()).get();
                deleteButton.addClickListener(buttonClickEvent -> {
                    modalDeleteItem(dialog, finalItem);
                });

                Field finalField = tClass.getDeclaredField(field.getName());
                AutoConfigurableField finalComponentWithValue = componentWithValue;
                dialogSaveButton.addClickListener(buttonClickEvent -> {
                    try {
                        finalField.setAccessible(true);
                        finalField.set(finalItem, finalComponentWithValue.getValue());
                        finalField.setAccessible(false);

                        T changed = service.save(finalItem);

                        customPostUpdate(changed);

                    } catch (Exception e) {
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
                buttonsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

                buttonsLayout.add(new HorizontalLayout(clipboardHelper), deleteButton);

                T finalItem2 = item;
                deleteButton.addClickListener(buttonClickEvent -> {
                    modalDeleteItem(dialog, finalItem2);
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

    @Override
    protected T customizeSavingInCreateForm(T newItem) {
        try {
            DTOMapper dtoMapper = new DTOMapper(bervanLogger, new ArrayList<>());
            return (T) dtoMapper.map(((BaseDTO) newItem));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void buildNewItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout) {
        try {
            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            VerticalLayout formLayout = new VerticalLayout();
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
            buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            buttonsLayout.add(dialogSaveButton);

            dialogSaveButton.addClickListener(buttonClickEvent -> {
                try {
                    T newObject = (T) dtoClass.getConstructor().newInstance();

                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(newObject, getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    newObject = customizeSavingInCreateForm(newObject);

                    service.save(newObject);

                    postSaveActions();

                    refreshData();
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


    @Override
    protected List<Field> getVaadinTableFields() {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .toList();
    }

    @Override
    protected Optional<Field> getVaadinTableField(String clickedColumnKey) {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinBervanColumn.class))
                .filter(e -> e.getAnnotation(VaadinBervanColumn.class).internalName().equals(clickedColumnKey))
                .findFirst();
    }
}