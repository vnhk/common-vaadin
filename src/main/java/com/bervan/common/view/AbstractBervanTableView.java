package com.bervan.common.view;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.component.table.builders.ColumnForGridBuilder;
import com.bervan.common.component.table.builders.ImageColumnGridBuilder;
import com.bervan.common.component.table.builders.LocalDateTimeBuilder;
import com.bervan.common.component.table.builders.TextColumnGridBuilder;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.service.BaseService;
import com.bervan.common.service.GridActionService;
import com.bervan.core.model.BervanLogger;
import com.bervan.ieentities.ExcelIEEntity;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final List<ColumnForGridBuilder> columnGridBuilders = new ArrayList<>(Arrays.asList(
            LocalDateTimeBuilder.getInstance(),
            ImageColumnGridBuilder.getInstance(),
            TextColumnGridBuilder.getInstance()
    ));
    protected final Button currentPage = new BervanButton(":)");
    protected final Button prevPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_LEFT));
    protected final Button nextPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_RIGHT));
    protected final BervanComboBox<Integer> goToPage = new BervanComboBox<>();
    protected final H4 selectedItemsCountLabel = new H4("Selected 0 item(s)");
    protected final List<Checkbox> checkboxes = new ArrayList<>();
    protected final List<Button> buttonsForCheckboxesForVisibilityChange = new ArrayList<>();
    protected final Set<String> currentlySortedColumns = new HashSet<>();
    protected int pageNumber = 0;
    protected int maxPages = 0;
    protected long allFound = 0;
    protected int pageSize = 50;
    protected HorizontalLayout paginationBar;
    protected Grid<T> grid;
    protected Span countItemsInfo = new Span("");
    protected boolean checkboxesColumnsEnabled = true;
    protected Checkbox selectAllCheckbox;
    protected SortDirection sortDirection = null;
    protected com.bervan.common.search.model.SortDirection sortDir = com.bervan.common.search.model.SortDirection.ASC; //move this logic to base service
    protected Grid.Column<T> columnSorted = null;
    protected AbstractFiltersLayout<ID, T> filtersLayout;
    protected BervanTableToolbar<ID, T> tableToolbarActions;
    protected String sortField;
    @Value("${file.service.storage.folder}")
    protected String pathToFileStorage;
    @Value("${global-tmp-dir.file-storage-relative-path}")
    protected String globalTmpDir;
    protected BervanLogger bervanLogger;
    protected GridActionService<ID, T> gridActionService;
    protected final Button applyFiltersButton = new BervanButton(new Icon(VaadinIcon.SEARCH), e -> applyCombinedFilters());
    protected final Button refreshTable = new BervanButton(new Icon(VaadinIcon.REFRESH), e -> {
        refreshData();
    });

    public AbstractBervanTableView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, BervanLogger bervanLogger, Class<T> tClass) {
        super(pageLayout, service, tClass);
        this.filtersLayout = buildFiltersLayout(tClass);
        this.bervanLogger = bervanLogger;
        addClassName("bervan-table-view");
        countItemsInfo.addClassName("table-pageable-details");
    }

    public static void addColumnForGridBuilder(ColumnForGridBuilder columnGridBuilder) {
        if (columnGridBuilder != null && !columnGridBuilders.contains(columnGridBuilder)) {
            columnGridBuilders.add(columnGridBuilders.size() - 1, columnGridBuilder); //default needs to be last
        }
    }

    public static void removeColumnForGridBuilder(ColumnForGridBuilder columnGridBuilder) {
        columnGridBuilders.remove(columnGridBuilder);
    }

    protected AbstractFiltersLayout<ID, T> buildFiltersLayout(Class<T> tClass) {
        return new AbstractFiltersLayout<>(tClass, applyFiltersButton, new DefaultFilterValuesContainer(new HashMap<>()));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
//        data.addAll(loadData());
    }

    protected void refreshData() {
        gridActionService.refreshData(data);
    }


    @Override
    public void renderCommonComponents() {
        grid = getGrid();
        grid.setItems(data);
        grid.addClassName("bervan-table");
        grid.addClassName("modern-table");
        grid.addItemClickListener(this::doOnColumnClick);
        grid.getColumns().forEach(column -> column.setClassNameGenerator(item -> "top-aligned-cell"));

        addButton.addClassName("option-button");
        addButton.addClassName("primary-action-button");

        currentPage.addClassName("option-button");
        currentPage.addClassName("page-indicator-button");

        prevPageButton.addClassName("option-button");
        prevPageButton.addClassName("page-navigation-button");
        prevPageButton.addClassName("prev-button");
        prevPageButton.addClickListener(e -> {
            if (pageNumber > 0) {
                pageNumber--;
                refreshData();
            }
        });

        nextPageButton.addClassName("option-button");
        nextPageButton.addClassName("page-navigation-button");
        nextPageButton.addClassName("next-button");
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

        goToPage.setMaxWidth("120px");
        goToPage.addClassName("page-select");

        HorizontalLayout topTableActions = new HorizontalLayout();
        topTableActions.addClassName("table-actions-bar");
        refreshTable.addClassName("refresh-button");
        topTableActions.add(new VerticalLayout(refreshTable));

        selectedItemsCountLabel.setVisible(checkboxesColumnsEnabled);
        selectedItemsCountLabel.addClassName("selection-counter");

        applyFiltersButton.addClassName("option-button");
        applyFiltersButton.addClassName("filter-apply-button");

        topLayout.add(filtersLayout.filtersButton);

        paginationBar = new HorizontalLayout(prevPageButton, currentPage, nextPageButton, goToPage);
        paginationBar.addClassName("pagination-bar");
        paginationBar.setAlignItems(Alignment.CENTER);
        paginationBar.setJustifyContentMode(JustifyContentMode.CENTER);

        countItemsInfo.addClassName("table-info-text");

        contentLayout.setSpacing(false);
        contentLayout.setPadding(false);
        contentLayout.add(topLayout, filtersLayout, countItemsInfo, topTableActions, grid, selectedItemsCountLabel, paginationBar, addButton);

        if(pageLayout != null) {
            add(pageLayout);
        }

        add(contentLayout);

        this.gridActionService = new GridActionService<>(service, filtersLayout, grid, (V) -> loadData());
        refreshData();

        buildToolbarActionBar();
        tableToolbarActions.setVisible(checkboxesColumnsEnabled);

        topTableActions.add(tableToolbarActions);
    }

    public void buildGridAutomatically(Grid<T> grid) {
        if (checkboxesColumnsEnabled) {
            buildSelectAllCheckboxesComponent();

            grid.addColumn(createCheckboxComponent())
                    .setHeader(selectAllCheckbox)
                    .setKey(CHECKBOX_COLUMN_KEY)
                    .setWidth("10px")
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

            for (ColumnForGridBuilder columnGridBuilder : columnGridBuilders) {
                if (columnGridBuilder.supports(config.getExtension(), config, tClass)) {
                    grid.addColumn(columnGridBuilder.build(vaadinTableColumn, config))
                            .setHeader(columnName)
                            .setKey(columnInternalName)
                            .setResizable(columnGridBuilder.isResizable())
                            .setSortable(columnGridBuilder.isSortable());
                    break;
                }
            }
        }

        // Modern grid styling
        grid.getElement().getStyle().set("--lumo-size-m", "16px");
        grid.setAllRowsVisible(false);
        grid.setPageSize(pageSize);

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
        this.selectAllCheckbox.addClassName("modern-table-checkbox");
        this.selectAllCheckbox.addClassName("select-all-checkbox");
        this.selectAllCheckbox.addValueChangeListener(clickEvent -> {
            if (clickEvent.isFromClient()) {
                if (checkboxes.isEmpty()) {
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

    protected ComponentRenderer<Checkbox, T> createCheckboxComponent() {
        return new ComponentRenderer<>(Checkbox::new, checkboxColumnUpdater());
    }

    private SerializableBiConsumer<Checkbox, T> checkboxColumnUpdater() {
        return (checkbox, record) -> {
            try {
                ID id = record.getId();
                checkbox.setId("checkbox-" + id);
                checkbox.setClassName("modern-table-checkbox");
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

    protected Set<String> getSelectedItemsByCheckbox() {
        return gridActionService.getSelectedItemsByCheckbox(checkboxes);
    }

    protected void buildToolbarActionBar() {
        tableToolbarActions = new BervanTableToolbar<>(gridActionService, checkboxes, data, tClass, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange)
                .withEditButton(service, bervanLogger)
                .withDeleteButton()
                .withExportButton(isExportable(), service, bervanLogger, pathToFileStorage, globalTmpDir)
                .build();
    }

    protected boolean isExportable() {
        return tClass != null && ExcelIEEntity.class.isAssignableFrom(tClass);
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
        checkboxes.removeAll(checkboxes);
        updateSelectedItemsLabel();
        try {
            SearchRequest request = filtersLayout.buildCombinedFilters();

            if (columnSorted != null && sortDirection != null) {
                sortField = columnSorted.getKey();
                if (sortDirection != SortDirection.ASCENDING) {
                    this.sortDir = com.bervan.common.search.model.SortDirection.DESC;
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

    protected void removeFilters() {
        filtersLayout.removeFilters();
        refreshData();
    }

    private void applyCombinedFilters() {
        pageNumber = 0;
        refreshData();
    }

    protected Grid<T> getGrid() {
        Grid<T> grid = new Grid<>(tClass, false);
        buildGridAutomatically(grid);

        return grid;
    }

    private void updateSelectedItemsLabel() {
        selectedItemsCountLabel.setText("Selected " + checkboxes.stream().filter(AbstractField::getValue).count() + " item(s)");
    }

    protected void customizeTextColumnUpdater(Span span, T record, Field f) {

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

    protected void deleteItemsFromGrid(List<T> items) {
        gridActionService.deleteItemsFromGrid(data, items, checkboxes);
    }

    protected void removeItemFromGrid(T item) {
        gridActionService.removeItemFromGrid(item, data, checkboxes);
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
                AutoConfigurableField componentWithValue = componentHelper.buildComponentForField(field, null);
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