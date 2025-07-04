package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.bervan.common.model.VaadinImageBervanColumn;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

@Slf4j
public abstract class AbstractBervanTableView<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractBervanEntityView<ID, T> implements AfterNavigationObserver {
    protected static final String CHECKBOX_COLUMN_KEY = "checkboxColumnKey";
    protected final Button currentPage = new BervanButton(":)");
    protected final Button prevPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_LEFT));
    protected final Button nextPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_RIGHT));
    protected final BervanComboBox<Integer> goToPage = new BervanComboBox<>();
    protected final H4 selectedItemsCountLabel = new H4("Selected 0 item(s)");
    private final Set<String> currentlySortedColumns = new HashSet<>();
    protected int pageNumber = 0;
    protected int maxPages = 0;
    protected long allFound = 0;
    protected int pageSize = 50;
    protected Grid<T> grid;
    protected final Button applyFiltersButton = new BervanButton(new Icon(VaadinIcon.SEARCH), e -> applyCombinedFilters());
    protected Span countItemsInfo = new Span("");
    protected boolean checkboxesColumnsEnabled = true;
    protected Checkbox selectAllCheckbox;
    protected List<Checkbox> checkboxes = new ArrayList<>();
    protected Button checkboxDeleteButton;
    protected List<Button> buttonsForCheckboxesForVisibilityChange = new ArrayList<>();
    protected SortDirection sortDirection = null;
    protected Grid.Column<T> columnSorted = null;
    protected AbstractFiltersLayout<ID, T> filtersLayout;
    protected HorizontalLayout checkboxActions;
    protected String sortField;
    protected final Button refreshTable = new BervanButton(new Icon(VaadinIcon.REFRESH), e -> {
        refreshData();
    });

    public AbstractBervanTableView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, BervanLogger log, Class<T> tClass) {
        super(pageLayout, service, tClass);
        this.filtersLayout = new AbstractFiltersLayout<>(tClass, applyFiltersButton);

        addClassName("bervan-entity-view");
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

    @Override
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
                pageNumber--;
                refreshData();
            }
        });

        nextPageButton.addClassName("option-button");
        nextPageButton.addClickListener(event -> {
            if (pageNumber < maxPages - 1) {
                pageNumber++;
                refreshData();
            }
        });

        goToPage.addValueChangeListener(event -> {
            if (event.getValue() == null || !event.isFromClient()) {
                return;
            }
            pageNumber = event.getValue() - 1;
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
            SearchRequest request = filtersLayout.buildCombinedFilters();

            com.bervan.common.search.model.SortDirection sortDir = com.bervan.common.search.model.SortDirection.ASC;
            if (columnSorted != null && sortDirection != null) {
                sortField = columnSorted.getKey();
                if (sortDirection != SortDirection.ASCENDING) {
                    sortDir = com.bervan.common.search.model.SortDirection.DESC;
                }
            } else {
                sortField = "id";
            }
            customizePreLoad(request); //must be before pageable to be able to modify it
            //maybe move it before sorting? now its not working

            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            List<String> columnsToFetch = getFieldsToFetchForTable();
            List<T> collect = this.service.load(request, pageable, sortField, sortDir, columnsToFetch).stream().filter(e -> e.isDeleted() == null || !e.isDeleted())
                    .collect(Collectors.toList());

            postSearchUpdate(collect);

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

    protected void postSearchUpdate(List<T> collect) {

    }

    protected List<String> getFieldsToFetchForTable() {
        List<String> result = new ArrayList<>();
        for (Field vaadinTableColumn : getVaadinTableFields()) {
            if (Collection.class.isAssignableFrom(vaadinTableColumn.getType()) ||
                    Map.class.isAssignableFrom(vaadinTableColumn.getType())) {
                continue;
            }
            result.add(vaadinTableColumn.getName());
        }
        result.add("id");
        return result;
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

        List<Field> vaadinTableColumns = getVaadinTableFields();
        for (Field vaadinTableColumn : vaadinTableColumns) {
            VaadinBervanColumnConfig config = buildColumnConfig(vaadinTableColumn);
            String columnInternalName = config.getInternalName();
            String columnName = config.getDisplayName();

            if (!config.isInTable()) {
                continue;
            }

            if (config.getExtension() == VaadinImageBervanColumn.class) {
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
                        .setSortable(config.isSortable());
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

    private SerializableBiConsumer<Span, T> textColumnUpdater(Field f, VaadinBervanColumnConfig config) {
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

    private SerializableBiConsumer<Span, T> imageColumnUpdater(Field f, VaadinBervanColumnConfig config) {
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

    protected ComponentRenderer<Span, T> createTextColumnComponent(Field f, VaadinBervanColumnConfig config) {
        return new ComponentRenderer<>(Span::new, textColumnUpdater(f, config));
    }

    protected ComponentRenderer<Checkbox, T> createCheckboxComponent() {
        return new ComponentRenderer<>(Checkbox::new, checkboxColumnUpdater());
    }

    protected ComponentRenderer<Span, T> createImageColumnComponent(Field f, VaadinBervanColumnConfig config) {
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

    @Override
    protected void customPostUpdate(T changed) {
        super.customPostUpdate(changed);
        refreshTable.clickInClient();
        refreshTable.click();
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

    @Override
    protected void modalDeleteItem(Dialog dialog, T item) {
        deleteItemsFromGrid(Collections.singletonList(item));
        dialog.close();
        showSuccessNotification("Deleted successfully!");
    }

    private void deleteItemsFromGrid(List<T> items) {
        for (T item : items) {
            service.deleteById(item.getId()); //for deleting original
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

    protected void postSaveActions() {
        super.postSaveActions();
        refreshData();
    }

    protected T customizeSavingInCreateForm(T newItem) {
        return newItem;
    }
}