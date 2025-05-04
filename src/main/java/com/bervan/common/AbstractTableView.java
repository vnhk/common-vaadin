package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinImageTableColumn;
import com.bervan.common.model.VaadinTableColumn;
import com.bervan.common.model.VaadinTableColumnConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.vaadin.olli.ClipboardHelper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

public abstract class AbstractTableView<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractPageView implements AfterNavigationObserver {
    protected final List<T> data = new LinkedList<>();
    protected static final String CHECKBOX_COLUMN_KEY = "checkboxColumnKey";
    protected int pageNumber = 0;
    protected int maxPages = 0;
    protected long allFound = 0;
    protected int pageSize = 50;
    protected final Button currentPage = new BervanButton(":)");
    protected final Button prevPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_LEFT));
    protected final Button nextPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_RIGHT));
    protected final BervanComboBox<Integer> goToPage = new BervanComboBox<>();
    protected final BaseService<ID, T> service;
    protected Grid<T> grid;
    protected MenuNavigationComponent pageLayout;
    protected final Button addButton = new BervanButton(new Icon(VaadinIcon.PLUS), e -> newItemButtonClick());
    protected final VerticalLayout contentLayout = new VerticalLayout();
    private final Set<String> currentlySortedColumns = new HashSet<>();
    protected final BervanLogger log;
    protected final Class<T> tClass;
    protected Span countItemsInfo = new Span("");
    private int amountOfWysiwygEditors = 0;

    protected boolean checkboxesColumnsEnabled = true;
    protected Checkbox selectAllCheckbox;
    protected List<Checkbox> checkboxes = new ArrayList<>();
    protected Button checkboxDeleteButton;
    protected List<Button> buttonsForCheckboxesForVisibilityChange = new ArrayList<>();
    protected final Button refreshTable = new BervanButton(new Icon(VaadinIcon.REFRESH), e -> {
        lastAction = AbstractTableAction.REFRESH_TABLE;
        refreshData();
    });
    protected final Button applyFiltersButton = new BervanButton(new Icon(VaadinIcon.SEARCH), e -> applyCombinedFilters());
    protected SortDirection sortDirection = null;
    protected Grid.Column<T> columnSorted = null;
    protected AbstractTableAction lastAction;
    protected AbstractFiltersLayout<ID, T> filtersLayout;
    protected HorizontalLayout topLayout = new HorizontalLayout();
    protected HorizontalLayout checkboxActions;
    protected final H4 selectedItemsCountLabel = new H4("Selected 0 item(s)");

    public AbstractTableView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, BervanLogger log, Class<T> tClass) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.log = log;
        this.tClass = tClass;
        this.filtersLayout = new AbstractFiltersLayout<>(tClass, applyFiltersButton);

        addClassName("bervan-table-view");
        countItemsInfo.addClassName("table-pageable-details");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
//        data.addAll(loadData());
    }

    protected Set<String> getSelectedItemsByCheckbox() {
        return checkboxes.stream()
                .filter(AbstractField::getValue)
                .map(Component::getId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(e -> e.split("checkbox-")[1])
                .collect(Collectors.toSet());
    }

    public void renderCommonComponents() {
        grid = getGrid();
        grid.setItems(data);
        grid.addClassName("bervan-table");
        grid.addItemClickListener(this::doOnColumnClick);
        grid.getColumns().forEach(column -> column.setClassNameGenerator(item -> "top-aligned-cell"));

        addButton.addClassName("option-button");
        currentPage.addClassName("option-button");
        currentPage.addClassName("option-button-warning");

        prevPageButton.addClassName("option-button");
        prevPageButton.addClickListener(e -> {
            if (pageNumber > 0) {
                lastAction = AbstractTableAction.PAGE_CHANGE;
                pageNumber--;
                refreshData();
            }
        });

        nextPageButton.addClassName("option-button");
        nextPageButton.addClickListener(event -> {
            if (pageNumber < maxPages - 1) {
                pageNumber++;
                lastAction = AbstractTableAction.PAGE_CHANGE;
                refreshData();
            }
        });

        goToPage.addValueChangeListener(event -> {
            if (event.getValue() == null || !event.isFromClient()) {
                return;
            }
            pageNumber = event.getValue() - 1;
            lastAction = AbstractTableAction.PAGE_CHANGE;
            refreshData();
        });

        goToPage.setMaxWidth("100px");

        HorizontalLayout topTableActions = new HorizontalLayout();
        topTableActions.add(refreshTable);

        checkboxActions = new HorizontalLayout();
        checkboxActions.setVisible(checkboxesColumnsEnabled);
        selectedItemsCountLabel.setVisible(checkboxesColumnsEnabled);

        checkboxDeleteButton = new BervanButton("Delete", deleteEvent -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm Deletion");
            confirmDialog.setText("Are you sure you want to delete the selected items?");

            confirmDialog.setConfirmText("Delete");
            confirmDialog.setConfirmButtonTheme("error primary");
            confirmDialog.addConfirmListener(event -> {
                Set<String> itemsId = getSelectedItemsByCheckbox();

                List<T> toBeDeleted = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .toList();

                deleteItemsFromGrid(toBeDeleted);
                showSuccessNotification("Removed " + toBeDeleted.size() + " items");

                selectAllCheckbox.setValue(false);
                for (Button button : buttonsForCheckboxesForVisibilityChange) {
                    button.setEnabled(isAtLeastOneCheckboxSelected());
                }

                refreshData();
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        checkboxActions.add(checkboxDeleteButton);
        topTableActions.add(checkboxActions);

        buttonsForCheckboxesForVisibilityChange.add(checkboxDeleteButton);

        topLayout.add(filtersLayout.filtersButton);
        contentLayout.add(topLayout, filtersLayout, countItemsInfo, topTableActions, grid, selectedItemsCountLabel, new HorizontalLayout(JustifyContentMode.BETWEEN, prevPageButton, currentPage, nextPageButton, goToPage), addButton);

        add(pageLayout);

        add(contentLayout);

        refreshData();

        for (Button button : buttonsForCheckboxesForVisibilityChange) {
            button.setEnabled(false);
        }
    }

    private void updateCurrentPageText() {
        currentPage.setText("Page: " + (pageNumber + 1) + "/" + (maxPages));
        List<Integer> items = new ArrayList<>();
        items.add(maxPages);
        for (int i = 1; i < maxPages; i++) {
            items.add(i + 1);
        }
        goToPage.setItems(items);
        goToPage.setValue(pageNumber + 1);
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
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
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

    protected List<T> loadData() {
        checkboxes = new ArrayList<>();
        updateSelectedItemsLabel();
        try {
            SearchRequest request = new SearchRequest();

//            if (applyFilters) {
            request = filtersLayout.buildCombinedFilters();
//            }

            customizePreLoad(request);

            String sortField;
            com.bervan.common.search.model.SortDirection sortDir = com.bervan.common.search.model.SortDirection.ASC;
            if (columnSorted != null && sortDirection != null) {
                sortField = columnSorted.getKey();
                if (sortDirection != SortDirection.ASCENDING) {
                    sortDir = com.bervan.common.search.model.SortDirection.DESC;
                }
            } else {
                sortField = "id";
            }

            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            List<T> collect = this.service.load(request, pageable, sortField, sortDir).stream().filter(e -> e.isDeleted() == null || !e.isDeleted())
                    .collect(Collectors.toList());

            allFound = countAll(request, collect);
            maxPages = (int) Math.ceil((double) allFound / pageSize);

            reloadItemsCountInfo();
            updateCurrentPageText();

            return collect;

        } catch (Exception e) {
            log.error("Could not load table!", e);
            showErrorNotification("Unable to load table!");
        }
        return new ArrayList<>();
    }

    protected void customizePreLoad(SearchRequest request) {

    }

    protected long countAll(SearchRequest request, Collection<T> collect) {
        return this.service.loadCount(request);
    }

    protected void reloadItemsCountInfo() {
        countItemsInfo.setText("Items: " + allFound + ", pages: " + maxPages);
    }

    protected void refreshData() {
        this.data.removeAll(this.data);
        this.data.addAll(loadData());
        this.grid.getDataProvider().refreshAll();
        lastAction = null;
    }

    protected void filterTable() {
//        applyFilters = true;
        refreshData();
    }

    protected void removeFilters() {
//        applyFilters = false;
        filtersLayout.removeFilters();
        refreshData();
    }

    private void applyCombinedFilters() {
        pageNumber = 0;
        filterTable();
    }

    protected Grid<T> getGrid() {
        Grid<T> grid = new Grid<>(tClass, false);
        buildGridAutomatically(grid);

        return grid;
    }

    protected final void buildGridAutomatically(Grid<T> grid) {
        if (checkboxesColumnsEnabled) {
            buildSelectAllCheckboxesComponent();

            grid.addColumn(createCheckboxComponent())
                    .setHeader(selectAllCheckbox)
                    .setKey(CHECKBOX_COLUMN_KEY)
                    .setWidth("20px")
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setResizable(false)
                    .setSortable(false);
        }

        preColumnAutoCreation(grid);

        List<Field> vaadinTableColumns = getVaadinTableColumns();
        for (Field vaadinTableColumn : vaadinTableColumns) {
            VaadinTableColumnConfig config = buildColumnConfig(vaadinTableColumn);
            String columnInternalName = config.getInternalName();
            String columnName = config.getDisplayName();

            if (!config.isInTable()) {
                continue;
            }

            if (config.getExtension() == VaadinImageTableColumn.class) {
                grid.addColumn(createImageColumnComponent(vaadinTableColumn, config))
                        .setHeader(columnName)
                        .setKey(columnInternalName)
                        .setResizable(false)
                        .setSortable(false);
            } else {
                grid.addColumn(createTextColumnComponent(vaadinTableColumn, config))
                        .setHeader(columnName)
                        .setKey(columnInternalName)
                        .setResizable(true)
                        .setSortable(true);
            }
        }

        grid.getElement().getStyle().set("--lumo-size-m", 10 + "px");

        grid.addSortListener(event -> {
            List<GridSortOrder<T>> sortOrders = event.getSortOrder();
            if (!sortOrders.isEmpty()) {
                GridSortOrder<T> sortOrder = sortOrders.get(0);
                SortDirection sortDirection = sortOrder.getDirection();

                this.columnSorted = sortOrder.getSorted();
                this.sortDirection = sortDirection;
                this.refreshData();
            }
        });
    }

    protected void preColumnAutoCreation(Grid<T> grid) {

    }

    private void buildSelectAllCheckboxesComponent() {
        this.selectAllCheckbox = new Checkbox(false);
        this.selectAllCheckbox.addValueChangeListener(clickEvent -> {
            if (clickEvent.isFromClient()) {
                if (checkboxes.size() == 0) {
                    selectAllCheckbox.setValue(false);
                    return;
                }

                for (Checkbox checkbox : checkboxes) {
                    checkbox.setValue(selectAllCheckbox.getValue());
                }
                for (Button button : buttonsForCheckboxesForVisibilityChange) {
                    button.setEnabled(selectAllCheckbox.getValue());
                }
            }

            updateSelectedItemsLabel();
        });
    }

    private void updateSelectedItemsLabel() {
        selectedItemsCountLabel.setText("Selected " + checkboxes.stream().filter(AbstractField::getValue).count() + " item(s)");
    }

    private SerializableBiConsumer<Span, T> textColumnUpdater(Field f, VaadinTableColumnConfig config) {
        return (span, record) -> {
            try {
                span.setClassName("bervan-cell-component");
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

    private SerializableBiConsumer<Span, T> imageColumnUpdater(Field f, VaadinTableColumnConfig config) {
        return (span, record) -> {
            try {
                span.setClassName("bervan-cell-component");
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o instanceof Collection<?> && ((Collection<?>) o).size() > 0) {
                    Icon showEditorIcon = new Icon(VaadinIcon.SCATTER_CHART);
                    span.add(showEditorIcon);
                }
                customizeImageColumnUpdater(span, record, f);
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                showErrorNotification("Could not create column in table!");
            }
        };
    }

    private SerializableBiConsumer<Checkbox, T> checkboxColumnUpdater() {
        return (checkbox, record) -> {
            try {
                ID id = record.getId();
                checkbox.setId("checkbox-" + id);
                checkbox.setClassName("bervan-cell-component");
                checkbox.addValueChangeListener(e -> {
                    if (e.isFromClient()) {
                        for (Button button : buttonsForCheckboxesForVisibilityChange) {
                            button.setEnabled(isAtLeastOneCheckboxSelected());
                        }
                        updateSelectedItemsLabel();

                        boolean flag = checkbox.getValue();
                        for (Checkbox c : checkboxes) {
                            if (c.getValue() != flag) {
                                selectAllCheckbox.setValue(false); //at least one is not selected
                                return;
                            }
                        }

                        selectAllCheckbox.setValue(flag); //all are selected or all are not selected
                    }
                });
                checkboxes.add(checkbox);
            } catch (Exception e) {
                log.error("Could not create checkbox column in table!", e);
                showErrorNotification("Could not checkbox create column in table!");
            }
        };
    }

    public boolean isAtLeastOneCheckboxSelected() {
        return checkboxes.parallelStream().anyMatch(AbstractField::getValue);
    }

    protected void customizeImageColumnUpdater(Span span, T record, Field f) {
    }

    protected void customizeTextColumnUpdater(Span span, T record, Field f) {

    }

    protected ComponentRenderer<Span, T> createTextColumnComponent(Field f, VaadinTableColumnConfig config) {
        return new ComponentRenderer<>(Span::new, textColumnUpdater(f, config));
    }

    protected ComponentRenderer<Checkbox, T> createCheckboxComponent() {
        return new ComponentRenderer<>(Checkbox::new, checkboxColumnUpdater());
    }

    protected ComponentRenderer<Span, T> createImageColumnComponent(Field f, VaadinTableColumnConfig config) {
        return new ComponentRenderer<>(Span::new, imageColumnUpdater(f, config));
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


    protected AutoConfigurableField buildComponentForField(Field field, Object item) throws IllegalAccessException {
        AutoConfigurableField component = null;
        VaadinTableColumnConfig config = buildColumnConfig(field);

        field.setAccessible(true);
        Object value = item == null ? null : field.get(item);
        value = getInitValueForInput(field, item, config, value);

        if (config.getExtension() == VaadinImageTableColumn.class) {
            List<String> imageSources = new ArrayList<>();
            //
            if (String.class.getTypeName().equals(config.getTypeName())) {
                imageSources.add((String) value);
                component = new BervanImageController(imageSources);
            } else if (List.class.getTypeName().equals(config.getTypeName())) {
                if (value != null) {
                    imageSources.addAll((Collection<String>) value);
                }
                component = new BervanImageController(imageSources);
            }
        } else if (config.getStrValues().size() > 0) {
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
        } else if (boolean.class.getTypeName().equals(config.getTypeName())) {
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

    private AutoConfigurableField buildBooleanInput(Object value, String displayName) {
        BervanBooleanField checkbox = new BervanBooleanField();
        if (value != null) {
            checkbox.setValue((Boolean) value);
        }
        return checkbox;
    }

    private Object getInitValueForInput(Field field, Object item, VaadinTableColumnConfig config, Object value) throws IllegalAccessException {
        if (item == null) {
            if (!config.getDefaultValue().equals("")) {
                if (String.class.getTypeName().equals(config.getTypeName())) {
                    value = config.getDefaultValue();
                } else if (Integer.class.getTypeName().equals(config.getTypeName())) {
                    value = Integer.parseInt(config.getDefaultValue());
                } else if (Double.class.getTypeName().equals(config.getTypeName())) {
                    value = Double.parseDouble(config.getDefaultValue());
                }
            }
        } else {
            value = field.get(item);
        }
        return value;
    }

    protected void buildOnColumnClickDialogContent(Dialog dialog, VerticalLayout dialogLayout,
                                                   HorizontalLayout headerLayout, String clickedColumn, T item) {
        Field field = null;
        try {
            List<Field> declaredFields = getVaadinTableColumns();

            Optional<Field> fieldOptional = declaredFields.stream()
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).internalName().equals(clickedColumn))
                    .filter(e -> !e.getAnnotation(VaadinTableColumn.class).inEditForm())
                    .findFirst();

            Optional<Field> editableField = declaredFields.stream()
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).internalName().equals(clickedColumn))
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).inEditForm())
                    .findFirst();

            if (editableField.isPresent()) {
                field = editableField.get();
                AutoConfigurableField componentWithValue = buildComponentForField(field, item);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.add((Component) componentWithValue);
                customFieldInEditLayout(layoutForField, componentWithValue, clickedColumn, item);

                Button dialogSaveButton = new BervanButton("Save");

                Button deleteButton = new BervanButton("Delete Item");
                deleteButton.addClassName("option-button-warning");

                ClipboardHelper clipboardHelper = getClipboardHelper(field, item, clickedColumn, componentWithValue);

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
            } else if (fieldOptional.isPresent()) {
                field = fieldOptional.get();
                AutoConfigurableField componentWithValue = buildComponentForField(field, item);
                componentWithValue.setReadOnly(true);
                VerticalLayout layoutForField = new VerticalLayout();
                layoutForField.add((Component) componentWithValue);
                customFieldInEditLayout(layoutForField, componentWithValue, clickedColumn, item);

                Button deleteButton = new BervanButton("Delete Item");
                deleteButton.addClassName("option-button-warning");

                ClipboardHelper clipboardHelper = getClipboardHelper(field, item, clickedColumn, componentWithValue);

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

    private ClipboardHelper getClipboardHelper(Field field, T item, String clickedColumn, AutoConfigurableField componentWithValue) {
        String copyValue = getCopyValue(field, item, clickedColumn, componentWithValue);
        Button copyButton = new BervanButton(new Icon(VaadinIcon.COPY_O), e -> showPrimaryNotification("Value copied!"));
        ClipboardHelper clipboardHelper = new ClipboardHelper(copyValue, copyButton);

        if (copyValue == null) {
            clipboardHelper.setVisible(false);
        }
        return clipboardHelper;
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
        deleteItemsFromGrid(Collections.singletonList(item));
        dialog.close();
        showSuccessNotification("Deleted successfully!");
    }

    private void deleteItemsFromGrid(List<T> items) {
        for (T item : items) {
            service.delete(item);
            removeItemFromGrid(item);
        }

        this.grid.getDataProvider().refreshAll();
        resetTableResults();
    }

    protected void resetTableResults() {
        filtersLayout.removeFilters();
        filterTable();
    }

    protected void removeItemFromGrid(T item) {
        int oldSize = this.data.size();
        this.data.remove(item);
        if (oldSize == this.data.size()) {
            ID id = item.getId();
            this.data.removeIf(e -> e.getId().equals(id));
        }

        ID id = item.getId();
        if (id != null) {
            List<Checkbox> checkboxesToRemove = checkboxes.stream()
                    .filter(AbstractField::getValue)
                    .filter(e -> e.getId().isPresent())
                    .filter(e -> e.getId().get().equals("checkbox-" + id))
                    .toList();
            checkboxes.removeAll(checkboxesToRemove);
        }
    }

    protected void refreshDataAfterUpdate(T item) {
        removeItemFromGrid(item);
        filterTable();
    }

    private List<Field> getVaadinTableColumns() {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinTableColumn.class))
                .toList();
    }

    protected void customFieldInEditLayout(VerticalLayout layoutForField, AutoConfigurableField
            componentWithValue, String clickedColumn, T item) {

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

            Button dialogSaveButton = new BervanButton("Save");

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

                    service.save(newObject);
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

    protected void customFieldInCreateLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {

    }

    protected T customizeSavingInCreateForm(T newItem) {
        return newItem;
    }
}