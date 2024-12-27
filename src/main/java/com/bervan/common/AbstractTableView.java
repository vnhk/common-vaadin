package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import com.bervan.common.model.VaadinTableColumnConfig;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractTableView<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractPageView implements AfterNavigationObserver {
    protected final Set<T> data = new HashSet<>();
    protected final BaseService<ID, T> service;
    protected Grid<T> grid;
    protected MenuNavigationComponent pageLayout;
    protected Button addButton;
    protected final VerticalLayout contentLayout = new VerticalLayout();
    private final Set<String> currentlySortedColumns = new HashSet<>();
    protected final BervanLogger log;
    protected final Class<T> tClass;
    protected TextField searchField;
    protected Text countItemsInfo = new Text("");
    private int amountOfWysiwygEditors = 0;

    protected Button filtersButton;
    protected VerticalLayout filtersMenuLayout;
    protected Button applyFiltersButton;
    private final Map<Field, Map<Object, Checkbox>> filtersMap = new HashMap<>();

    public AbstractTableView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, BervanLogger log, Class<T> tClass) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.log = log;
        this.tClass = tClass;

        addClassName("bervan-table-view");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        data.addAll(loadData());
    }

    public void renderCommonComponents() {
        grid = getGrid();
        grid.setItems(data);
        grid.addClassName("bervan-table");
        grid.addItemClickListener(this::doOnColumnClick);
        grid.addItemDoubleClickListener(this::doOnColumnDoubleClick);
        grid.getColumns().forEach(column -> column.setClassNameGenerator(item -> "top-aligned-cell"));

        searchField = getFilter();

        filtersButton = new Button("Filters", e -> toggleFiltersMenu());
        filtersButton.addClassName("option-button");

        filtersMenuLayout = new VerticalLayout();
        filtersMenuLayout.setVisible(false);

        buildFiltersMenu();

        applyFiltersButton = new Button("Apply", e -> applyCombinedFilters());
        applyFiltersButton.addClassName("option-button");
        filtersMenuLayout.add(applyFiltersButton);

        addButton = new Button("Add New Element", e -> newItemButtonClick());
        addButton.addClassName("option-button");

        contentLayout.add(filtersButton, filtersMenuLayout, searchField, countItemsInfo, grid, addButton);
        add(pageLayout);
        add(contentLayout);
    }

    protected void doOnColumnDoubleClick(ItemDoubleClickEvent<T> tItemDoubleClickEvent) {

    }

    protected void removeUnSortedState(Grid<T> grid, int columnIndex) {
        grid.addSortListener(e -> {
            List<GridSortOrder<T>> sortOrderList = e.getSortOrder();
            List<String> notFound = new ArrayList<>(
                    currentlySortedColumns);
            for (GridSortOrder<T> sortOrder : sortOrderList) {
                String key = sortOrder.getSorted().getKey();
                currentlySortedColumns.add(key);
                notFound.remove(key);
            }
            if (!notFound.isEmpty()) {
                for (String key : notFound) {
                    sortOrderList.add(columnIndex, new GridSortOrder<>(
                            grid.getColumnByKey(key), SortDirection.ASCENDING));
                }
                grid.sort(sortOrderList);
            }
        });
    }

    protected Set<T> loadData() {
        try {
            Set<T> collect = this.service.load().stream().filter(e -> e.getDeleted() == null || !e.getDeleted())
                    .collect(Collectors.toSet());
            reloadItemsCountInfo(collect);
            return collect;

        } catch (Exception e) {
            log.error("Could not load table!");
            showErrorNotification("Unable to load table!");
        }
        return new HashSet<>();
    }

    protected void reloadItemsCountInfo(Collection<T> collect) {
        countItemsInfo.setText("Items: " + collect.size());
    }

    protected void refreshData() {
        this.data.removeAll(this.data);
        this.data.addAll(loadData());
        this.grid.getDataProvider().refreshAll();
    }

    protected void filterTable(String filterText) {
        if ((filterText == null || filterText.isEmpty()) && filtersMap.isEmpty()) {
            grid.setItems(data);
            reloadItemsCountInfo(data);
            return;
        }
        List<T> collect = data.stream()
                .filter(q -> filterByText(q, filterText))
                .collect(Collectors.toList());
        grid.setItems(collect);
        reloadItemsCountInfo(collect);
    }

    private boolean filterByText(T record, String filterText) {
        if (filterText == null || filterText.isEmpty()) return true;
        return record.getTableFilterableColumnValue().toLowerCase().contains(filterText.toLowerCase());
    }

    protected void filterTableWithCheckboxes(String filterText) {
        List<T> filtered = data.stream()
                .filter(q -> filterByText(q, filterText))
                .filter(this::filterBySelectedValues)
                .collect(Collectors.toList());
        grid.setItems(filtered);
        reloadItemsCountInfo(filtered);
    }

    private boolean filterBySelectedValues(T record) {
        for (Field field : filtersMap.keySet()) {
            VaadinTableColumnConfig config = buildColumnConfig(field);
            if (!filtersMap.get(field).isEmpty()) {
                Object fieldValue = null;
                try {
                    field.setAccessible(true);
                    fieldValue = field.get(record);
                    field.setAccessible(false);
                } catch (Exception ex) {
                    return false;
                }
                if (config.getStrValues().size() > 0) {
                    if (fieldValue instanceof String value) {
                        Checkbox c = filtersMap.get(field).get(value);
                        if (c != null && !c.getValue()) {
                            return false;
                        }
                    }
                }
                if (config.getIntValues().size() > 0) {
                    if (fieldValue instanceof Integer intVal) {
                        Checkbox c = filtersMap.get(field).get(intVal);
                        if (c != null && !c.getValue()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void buildFiltersMenu() {
        List<Field> fields = getVaadinTableColumns();
        for (Field field : fields) {
            VaadinTableColumnConfig config = buildColumnConfig(field);
            if (!config.getStrValues().isEmpty() || !config.getIntValues().isEmpty()) {
                VerticalLayout fieldLayout = new VerticalLayout();
                fieldLayout.setWidthFull();
                filtersMap.putIfAbsent(field, new HashMap<>());
                if (!config.getStrValues().isEmpty()) {
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    for (String val : config.getStrValues()) {
                        Checkbox checkbox = new Checkbox(val);
                        checkbox.setValue(true);
                        filtersMap.get(field).put(val, checkbox);
                        fieldLayout.add(checkbox);
                    }
                } else {
                    H4 label = new H4(config.getDisplayName() + ":");
                    fieldLayout.add(label);
                    for (Integer val : config.getIntValues()) {
                        Checkbox checkbox = new Checkbox(val.toString());
                        checkbox.setValue(true);
                        filtersMap.get(field).put(val, checkbox);
                        fieldLayout.add(checkbox);
                    }
                }
                filtersMenuLayout.add(fieldLayout);
            }
        }

        if (fields.size() == 0) {
            filtersButton.setVisible(false);
        }
    }

    private void toggleFiltersMenu() {
        filtersMenuLayout.setVisible(!filtersMenuLayout.isVisible());
    }

    private void applyCombinedFilters() {
        filterTableWithCheckboxes(searchField.getValue());
    }

    protected TextField getFilter() {
        TextField searchField = new TextField("Filter table...");
        searchField.setWidth("100%");
        searchField.addValueChangeListener(e -> filterTableWithCheckboxes(e.getValue()));
        return searchField;
    }

    protected Grid<T> getGrid() {
        Grid<T> grid = new Grid<>(tClass, false);
        buildGridAutomatically(grid);
        return grid;
    }

    protected final void buildGridAutomatically(Grid<T> grid) {
        List<Field> vaadinTableColumns = getVaadinTableColumns();
        for (Field vaadinTableColumn : vaadinTableColumns) {
            VaadinTableColumnConfig config = buildColumnConfig(vaadinTableColumn);
            String columnInternalName = config.getInternalName();
            String columnName = config.getDisplayName();
            grid.addColumn(createTextColumnComponent(vaadinTableColumn, config)).setHeader(columnName).setKey(columnInternalName)
                    .setResizable(true);
        }

        grid.getElement().getStyle().set("--lumo-size-m", 10 + "px");
    }

    private SerializableBiConsumer<Span, T> textColumnUpdater(Field f, VaadinTableColumnConfig config) {
        return (span, record) -> {
            try {
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o != null) {
                    if (config.isWysiwyg()) {
                        Icon showEditorIcon = new Icon(VaadinIcon.OPEN_BOOK);
                        span.add(showEditorIcon);
                    } else {
                        span.add(o.toString());
                    }
                }
                customizeTextColumnUpdater(span, record, f);
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                showErrorNotification("Could not create column in table!");
            }
        };
    }

    protected void customizeTextColumnUpdater(Span span, T record, Field f) {

    }

    protected ComponentRenderer<Span, T> createTextColumnComponent(Field f, VaadinTableColumnConfig config) {
        return new ComponentRenderer<>(Span::new, textColumnUpdater(f, config));
    }

    protected void doOnColumnClick(ItemClickEvent<T> event) {
        Dialog dialog = new Dialog();
        dialog.setWidth("80vw");
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);
        String clickedColumn = event.getColumn().getKey();

        buildOnColumnClickDialogContent(dialog, dialogLayout, headerLayout, clickedColumn, event.getItem());

        dialog.add(dialogLayout);
        dialog.open();
    }

    protected VaadinTableColumnConfig buildColumnConfig(Field field) {
        VaadinTableColumnConfig config = new VaadinTableColumnConfig();
        config.setField(field);
        config.setTypeName(field.getType().getTypeName());
        config.setDisplayName(field.getAnnotation(VaadinTableColumn.class).displayName());
        config.setInternalName(field.getAnnotation(VaadinTableColumn.class).internalName());
        config.setWysiwyg(field.getAnnotation(VaadinTableColumn.class).isWysiwyg());

        config.setStrValues(Arrays.stream(field.getAnnotation(VaadinTableColumn.class).strValues()).toList());
        config.setIntValues(Arrays.stream(field.getAnnotation(VaadinTableColumn.class).intValues()).boxed().collect(Collectors.toList()));

        return config;
    }

    protected AutoConfigurableField buildComponentForField(Field field, Object item) throws IllegalAccessException {
        AutoConfigurableField component = null;
        VaadinTableColumnConfig config = buildColumnConfig(field);

        field.setAccessible(true);
        Object value = item == null ? null : field.get(item);

        if (config.getStrValues().size() > 0) {
            BervanComboBox<String> comboBox = new BervanComboBox<>(config.getDisplayName());
            component = buildComponentForComboBox(config.getStrValues(), comboBox, ((String) value));
        } else if (config.getIntValues().size() > 0) {
            BervanComboBox<Integer> comboBox = new BervanComboBox<>(config.getDisplayName());
            component = buildComponentForComboBox(config.getIntValues(), comboBox, ((Integer) value));
        } else if (String.class.getTypeName().equals(config.getTypeName())) {
            component = buildTextArea(value, config.getDisplayName(), config.isWysiwyg());
        } else if (Integer.class.getTypeName().equals(config.getTypeName())) {
            component = buildIntegerInput(value, config.getDisplayName());
        } else if (Double.class.getTypeName().equals(config.getTypeName())) {
            component = buildDoubleInput(value, config.getDisplayName());
        } else if (LocalDateTime.class.getTypeName().equals(config.getTypeName())) {
            component = buildDateTimeInput(value, config.getDisplayName());
        } else {
            component = new BervanTextField("Not supported yet");
            component.setValue(value);
        }

        component.setId(config.getTypeName() + "_id");

        field.setAccessible(false);

        return component;
    }

    protected void buildOnColumnClickDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout, String clickedColumn, T item) {
        Field field = null;
        try {
            List<Field> declaredFields = getVaadinTableColumns();

            Optional<Field> fieldOptional = declaredFields.stream()
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).internalName().equals(clickedColumn))
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).inEditForm())
                    .findFirst();

            if (fieldOptional.isPresent()) {
                field = fieldOptional.get();
                AutoConfigurableField componentWithValue = buildComponentForField(field, item);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.add((Component) componentWithValue);
                customFieldInEditLayout(layoutForField, componentWithValue, clickedColumn, item);

                Button dialogSaveButton = new Button("Save");
                dialogSaveButton.addClassName("option-button");

                Button deleteButton = new Button("Delete Item");
                deleteButton.addClassName("option-button-warning");
                deleteButton.addClassName("option-button");

                HorizontalLayout buttonsLayout = new HorizontalLayout();
                buttonsLayout.setWidthFull();
                buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                buttonsLayout.add(dialogSaveButton, deleteButton);

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

                        customPreUpdate(clickedColumn, layoutForField, item, finalField, finalComponentWithValue);

                        T changed = service.save(item);

                        refreshDataAfterUpdate(changed);
                    } catch (IllegalAccessException e) {
                        log.error("Could not update field value!", e);
                        showErrorNotification("Could not update value!");
                    }
                    dialog.close();
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

    protected void customPreUpdate(String clickedColumn, VerticalLayout layoutForField, T item, Field finalField, AutoConfigurableField finalComponentWithValue) {

    }

    private static <X> AutoConfigurableField buildComponentForComboBox(List<X> values, BervanComboBox<X> comboBox, X initVal) {
        AutoConfigurableField componentWithValue;
        comboBox.setItems(values);
        comboBox.setWidth("100%");
        comboBox.setValue(initVal);
        componentWithValue = comboBox;
        return componentWithValue;
    }

    protected void modalDeleteItem(Dialog dialog, T item) {
        service.delete(item);
        removeItemFromGrid(item);
        this.grid.getDataProvider().refreshAll();
        resetTableResults();
        dialog.close();

        showSuccessNotification("Deleted successfully!");
    }

    protected void resetTableResults() {
        searchField.setValue("");
        filterTableWithCheckboxes("");
    }

    protected void removeItemFromGrid(T item) {
        int oldSize = this.data.size();
        this.data.remove(item);
        if (oldSize == this.data.size()) {
            ID id = item.getId();
            this.data.removeIf(e -> e.getId().equals(id));
        }
    }

    protected void refreshDataAfterUpdate(T item) {
        removeItemFromGrid(item);
        this.data.add(item);
        this.grid.setItems(this.data);
        this.grid.getDataProvider().refreshItem(item);
        this.grid.getDataProvider().refreshAll();
        filterTableWithCheckboxes(searchField.getValue());
    }

    private List<Field> getVaadinTableColumns() {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinTableColumn.class))
                .toList();
    }

    protected void customFieldInEditLayout(VerticalLayout layoutForField, AutoConfigurableField componentWithValue, String clickedColumn, T item) {

    }

    protected void customFieldInCreateLayout(Field field, VerticalLayout layoutForField, AutoConfigurableField componentWithValue) {

    }

    private AutoConfigurableField<LocalDateTime> buildDateTimeInput(Object value, String displayName) {
        BervanDateTimePicker dateTimePicker = new BervanDateTimePicker(displayName);
        dateTimePicker.setLabel("Select Date and Time");

        if (value != null)
            dateTimePicker.setValue((LocalDateTime) value);
        return dateTimePicker;
    }

    private AutoConfigurableField<Integer> buildIntegerInput(Object value, String displayName) {
        BervanIntegerField field = new BervanIntegerField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue((Integer) value);
        return field;
    }

    private AutoConfigurableField<Double> buildDoubleInput(Object value, String displayName) {
        BervanDoubleField field = new BervanDoubleField(displayName);
        field.setWidthFull();
        if (value != null)
            field.setValue(((Double) value));
        return field;
    }

    private AutoConfigurableField<String> buildTextArea(Object value, String displayName, boolean isWysiwyg) {
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

    protected void buildNewItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout) {
        try {
            Map<Field, AutoConfigurableField> fieldsHolder = new HashMap<>();
            Map<Field, VerticalLayout> fieldsLayoutHolder = new HashMap<>();
            VerticalLayout formLayout = new VerticalLayout();
            List<Field> declaredFields = getVaadinTableColumns().stream()
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).inSaveForm())
                    .toList();

            for (Field field : declaredFields) {
                AutoConfigurableField componentWithValue = buildComponentForField(field, null);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.getThemeList().remove("spacing");
                layoutForField.getThemeList().remove("padding");
                layoutForField.add((Component) componentWithValue);
                customFieldInCreateLayout(field, layoutForField, componentWithValue);
                formLayout.add(layoutForField);
                fieldsHolder.put(field, componentWithValue);
                fieldsLayoutHolder.put(field, layoutForField);
            }

            customFieldInCreateLayout(fieldsHolder, fieldsLayoutHolder, formLayout);

            Button dialogSaveButton = new Button("Save");
            dialogSaveButton.addClassName("option-button");

            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setWidthFull();
            buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            buttonsLayout.add(dialogSaveButton);

            dialogSaveButton.addClickListener(buttonClickEvent -> {
                try {
                    T newObject = tClass.getConstructor().newInstance();
                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(newObject, fieldAutoConfigurableFieldEntry.getValue().getValue());
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    newObject = customizeSavingInCreateForm(newObject);

                    newObject = service.save(newObject);
                    this.data.add(newObject);
                    this.grid.getDataProvider().refreshAll();
                } catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
                         InvocationTargetException e) {
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

    protected void customFieldInCreateLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {

    }

    protected T customizeSavingInCreateForm(T newItem) {
        return newItem;
    }
}