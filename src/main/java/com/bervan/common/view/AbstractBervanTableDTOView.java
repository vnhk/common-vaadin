package com.bervan.common.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanButton;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
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

    public AbstractBervanTableDTOView(MenuNavigationComponent pageLayout, BaseService<ID, T> service, BervanLogger log, Class<T> tClass, Class<DTO> dtoClass, BervanViewConfig bervanViewConfig) {
        super(pageLayout, service, log, bervanViewConfig, tClass);
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
            Optional<Field> vaadinTableField = getVaadinTableField(clickedField);
            if (vaadinTableField.isEmpty()) {
                throw new RuntimeException("Invalid config or implementation for: " + clickedField);
            }

            Optional<ClassViewAutoConfigColumn> fieldConfig = getFieldConfig(clickedField);
            if (fieldConfig.isEmpty()) {
                throw new RuntimeException("Invalid config or implementation for: " + clickedField);
            }

            ClassViewAutoConfigColumn classViewAutoConfigColumn = fieldConfig.get();
            field = vaadinTableField.get();

            DTOMapper dtoMapper = new DTOMapper(bervanLogger, new ArrayList<>());
            if (classViewAutoConfigColumn.isInEditForm()) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(bervanViewConfig, field, item, false);
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
            } else if (!classViewAutoConfigColumn.isInEditForm()) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(bervanViewConfig, field, item, true);
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
    protected T preSaveActions(T newItem) {
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
            List<Field> declaredFields = getVaadinTableFieldsForSaveForm();

            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(bervanViewConfig, field, null, false);
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
                        fieldAutoConfigurableFieldEntry.getKey().set(newObject, componentHelper.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    newObject = preSaveActions(newObject);

                    T save = service.save(newObject);

                    postSaveActions(save);

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
        Set<String> fieldNames = bervanViewConfig.getFieldNames(dtoClass);
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(e -> fieldNames.contains(e.getName()))
                .toList();
    }

    @Override
    protected Optional<Field> getVaadinTableField(String clickedColumnKey) {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(bervanViewConfig::isAutoConfigurableField)
                .filter(e -> bervanViewConfig.getInternalName(e).equals(clickedColumnKey))
                .findFirst();
    }

    @Override
    protected Optional<ClassViewAutoConfigColumn> getFieldConfig(String clickedColumnKey) {
        return Arrays.stream(dtoClass.getDeclaredFields())
                .filter(bervanViewConfig::isAutoConfigurableField)
                .filter(e -> bervanViewConfig.getInternalName(e).equals(clickedColumnKey))
                .map(bervanViewConfig::getFieldConfig)
                .findFirst();
    }
}