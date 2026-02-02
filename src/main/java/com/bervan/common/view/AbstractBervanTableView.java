package com.bervan.common.view;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.component.table.BervanFloatingToolbar;
import com.bervan.common.component.table.BervanPageSizeSelector;
import com.bervan.common.component.table.builders.*;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.table.BervanTableConfig;
import com.bervan.common.view.table.BervanTableState;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.logging.BaseProcessContext;
import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.bervan.common.TableClassUtils.buildColumnConfig;

@CssImport("./bervan-glassmorphism.css")
@CssImport("./bervan-table.css")
@CssImport("./bervan-toolbar.css")
@CssImport("./bervan-variables.css")
public abstract class AbstractBervanTableView<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractBervanEntityView<ID, T> implements AfterNavigationObserver {
    protected static final String CHECKBOX_COLUMN_KEY = "checkboxColumnKey";
    private static final List<ColumnForGridBuilder> columnGridBuilders = new ArrayList<>(Arrays.asList(
            LocalDateTimeBuilder.getInstance(),
            JsonLogColumnGridBuilder.getInstance(),
            ImageColumnGridBuilder.getInstance(),
            TextColumnGridBuilder.getInstance()
    ));
    protected final Div gridLoadingOverlay = new Div();
    protected final Button currentPage = new BervanButton(":)");
    protected final Button prevPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_LEFT));
    protected final Button nextPageButton = new BervanButton(new Icon(VaadinIcon.ARROW_RIGHT));
    protected final BervanComboBox<Integer> goToPage = new BervanComboBox<>();
    protected final H4 selectedItemsCountLabel = new H4("Selected 0 item(s)");
    protected final List<Checkbox> checkboxes = new ArrayList<>();
    protected final List<Button> buttonsForCheckboxesForVisibilityChange = new ArrayList<>();
    protected final Set<String> currentlySortedColumns = new HashSet<>();
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");
    protected int pageNumber = 0;
    protected int maxPages = 0;
    protected long allFound = 0;
    protected int pageSize = 50;
    protected HorizontalLayout paginationBar;
    protected Grid<T> grid;
    protected H4 countItemsInfo = new H4("");
    protected boolean checkboxesColumnsEnabled = true;
    protected Checkbox selectAllCheckbox;
    protected SortDirection sortDirection = null;
    protected com.bervan.common.search.model.SortDirection sortDir = com.bervan.common.search.model.SortDirection.ASC; //move this logic to base service
    protected Grid.Column<T> columnSorted = null;
    protected AbstractFiltersLayout<ID, T> filtersLayout;
    protected BervanTableToolbar<ID, T> tableToolbarActions;
    protected HorizontalLayout topTableActions;
    protected String sortField;
    @Value("${file.service.storage.folder.main}")
    protected String pathToFileStorage;
    @Value("${global-tmp-dir.file-storage-relative-path}")
    protected String globalTmpDir;
    protected ProgressBar gridProgressBar = new ProgressBar();
    protected String processName;
    protected final Button applyFiltersButton = new BervanButton(new Icon(VaadinIcon.SEARCH), e -> applyCombinedFilters());
    protected final Button refreshTable = new BervanButton(new Icon(VaadinIcon.REFRESH), e -> {
        refreshData();
    });
    protected boolean searchBarVisible = true;
    // Modern table features
    protected BervanTableConfig tableConfig;
    protected BervanTableState tableState;
    protected BervanFloatingToolbar floatingToolbar;
    protected BervanPageSizeSelector pageSizeSelector;
    protected Map<String, Grid.Column<T>> columnMap = new HashMap<>();

    /**
     * Original constructor - maintains backward compatibility.
     * All modern features are disabled by default.
     */
    public AbstractBervanTableView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service, BervanViewConfig bervanViewConfig, Class<T> tClass) {
        this(pageLayout, service, bervanViewConfig, tClass, BervanTableConfig.defaults());
    }

    /**
     * New constructor for modern features.
     * Pass a BervanTableConfig to enable specific features.
     */
    public AbstractBervanTableView(MenuNavigationComponent pageLayout, @Autowired BaseService<ID, T> service,
                                   BervanViewConfig bervanViewConfig, Class<T> tClass, BervanTableConfig tableConfig) {
        super(pageLayout, service, bervanViewConfig, tClass);
        this.tableConfig = BervanTableConfig.builder()
                .enableAllModernFeatures()
                .build();
        this.filtersLayout = buildFiltersLayout(tClass);
        addClassName("bervan-table-view");
        countItemsInfo.setClassName("selection-counter");

        gridProgressBar.setWidth("800px");
        gridProgressBar.setIndeterminate(true);

        // Initialize modern features based on config
        initializeModernFeatures();
    }

    public static void addColumnForGridBuilder(ColumnForGridBuilder columnGridBuilder) {
        if (columnGridBuilder != null && !columnGridBuilders.contains(columnGridBuilder)) {
            columnGridBuilders.add(columnGridBuilders.size() - 1, columnGridBuilder); //default needs to be last
        }
    }

    public static void removeColumnForGridBuilder(ColumnForGridBuilder columnGridBuilder) {
        columnGridBuilders.remove(columnGridBuilder);
    }

    /**
     * Initializes modern features based on the table configuration.
     */
    protected void initializeModernFeatures() {
        if (!tableConfig.hasAnyModernFeature()) {
            return;
        }

        // Apply glassmorphism styling
        if (tableConfig.isGlasmorphismEnabled()) {
            addClassName("bervan-modern-table");
            addClassName("bervan-glass");
        }

        // Initialize page size from config
        if (tableConfig.isPageSizeSelectorEnabled()) {
            pageSize = tableConfig.getDefaultPageSize();
        }

        // Initialize table state for persistence
        String stateKey = tableConfig.getStateKeyPrefix() + "-" + getClass().getSimpleName();
        tableState = new BervanTableState(getClass().getSimpleName(), tableConfig.getStateKeyPrefix());
    }

    protected void showGridLoadingProgress(boolean show) {
        gridProgressBar.setVisible(show);
    }

    protected AbstractFiltersLayout<ID, T> buildFiltersLayout(Class<T> tClass) {
        return new AbstractFiltersLayout<>(tClass, applyFiltersButton, new DefaultFilterValuesContainer(new HashMap<>()), bervanViewConfig);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
//        data.addAll(loadData());
    }

    protected void refreshData() {
        showGridLoadingProgress(true);

        SecurityContext context = SecurityContextHolder.getContext();
        UI current = UI.getCurrent();

        CompletableFuture.runAsync(() -> {
            try {
                SecurityContextHolder.setContext(context);
                data.removeAll(data);
                data.addAll(loadData());
                current.access(() -> {
                    reloadItemsCountInfo();
                    updateCurrentPageText();
                    showGridLoadingProgress(false);
                    grid.setItems(data);
                    grid.getDataProvider().refreshAll();
                    updateSelectedItemsLabel();
                    hideFloatingToolbar();
                });

            } catch (Exception e) {
                log.error(buildContext(), "Error while refreshing grid data", e);
            } finally {
                showGridLoadingProgress(false);
            }
        });
    }

    private Map.Entry<String, Map<String, Object>> buildContext() {
        BaseProcessContext baseProcessContext = BaseProcessContext.builder()
                .processName(processName)
                .route(pageLayout.getCurrentRouteName())
                .build();
        return baseProcessContext.map();
    }

    @Override
    public void renderCommonComponents() {
        grid = getGrid();
        grid.setItems(data);
        grid.addClassName("bervan-table");
        grid.addClassName("modern-table");
        grid.addItemClickListener(this::doOnColumnClick);
        grid.getColumns().forEach(column -> column.setClassNameGenerator(item -> "top-aligned-cell"));

        // Apply modern styling if enabled
        if (tableConfig.isGlasmorphismEnabled()) {
            grid.addClassName("bervan-modern-table");
        }

        newItemButton.addClassName("bervan-icon-btn");
        newItemButton.addClassName("primary");
        newItemButton.getElement().setAttribute("title", "Add new item");

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

        topTableActions = new HorizontalLayout();
        topTableActions.addClassName("table-actions-bar");
        topTableActions.setWidthFull();
        topTableActions.setJustifyContentMode(JustifyContentMode.CENTER);
        topTableActions.setAlignItems(Alignment.CENTER);
        refreshTable.addClassName("bervan-icon-btn");
        refreshTable.getElement().setAttribute("title", "Refresh");

        // Filters expand/collapse button in toolbar
        filtersLayout.filtersButton.addClassName("bervan-icon-btn");
        filtersLayout.filtersButton.getElement().setAttribute("title", "Toggle Filters");
        topTableActions.add(filtersLayout.filtersButton, refreshTable);

        selectedItemsCountLabel.setVisible(checkboxesColumnsEnabled);
        selectedItemsCountLabel.addClassName("selection-counter");

        applyFiltersButton.addClassName("option-button");
        applyFiltersButton.addClassName("filter-apply-button");

        paginationBar = new HorizontalLayout(prevPageButton, currentPage, nextPageButton, goToPage);
        paginationBar.addClassName("pagination-bar");
        paginationBar.setAlignItems(Alignment.CENTER);
        paginationBar.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add page size selector if enabled
        if (tableConfig.isPageSizeSelectorEnabled()) {
            addPageSizeSelector(paginationBar);
        }

        // Info row below grid: selected items (left) and items count (right)
        HorizontalLayout belowGridInfoLayout = new HorizontalLayout(selectedItemsCountLabel, countItemsInfo);
        belowGridInfoLayout.setWidthFull();
        belowGridInfoLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        belowGridInfoLayout.setAlignItems(Alignment.CENTER);
        belowGridInfoLayout.getStyle().set("padding", "2px 16px");
        belowGridInfoLayout.getStyle().set("margin-top", "-4px");

        gridLoadingOverlay.add(gridProgressBar);
        gridLoadingOverlay.setHeight("20px");
        contentLayout.setSpacing(false);
        contentLayout.setPadding(false);

        if (searchBarVisible) {
            contentLayout.add(filtersLayout, topTableActions, gridLoadingOverlay,
                    filtersLayout.allFieldsTextSearch,
                    grid, belowGridInfoLayout, paginationBar);
        } else {
            contentLayout.add(filtersLayout, topTableActions, gridLoadingOverlay,
                    grid, belowGridInfoLayout, paginationBar);
        }


        if (pageLayout != null) {
            add(pageLayout);
        }

        add(contentLayout);

        // Add floating toolbar if enabled
        if (tableConfig.isFloatingToolbarEnabled()) {
            addFloatingToolbar();
        }

        topTableActions.add(newItemButton);

        // Hook for subclasses to add custom buttons to topTableActions
        customizeTopTableActions(topTableActions);

        buildToolbarActionBar();

        if (tableConfig.isFloatingToolbarEnabled()) {
            tableToolbarActions.setVisible(false);
        } else {
            tableToolbarActions.setVisible(checkboxesColumnsEnabled);
            topTableActions.add(tableToolbarActions);
        }

        // Setup keyboard navigation if enabled
        if (tableConfig.isKeyboardNavigationEnabled()) {
            setupKeyboardNavigation();
        }

        // Load persisted state
        if (tableConfig.hasAnyModernFeature() && tableState != null) {
            loadPersistedState();
        }

        // Initial data load
        refreshData();
    }


    /**
     * Adds page size selector to the pagination bar.
     */
    protected void addPageSizeSelector(HorizontalLayout paginationBar) {
        pageSizeSelector = new BervanPageSizeSelector(tableConfig.getPageSizeOptions());
        pageSizeSelector.setValue(pageSize);
        pageSizeSelector.setStorageKey(tableConfig.getStateKeyPrefix() + "-pagesize-" + getClass().getSimpleName());
        pageSizeSelector.addPageSizeChangeListener(e -> {
            int newSize = e.getValue();
            if (newSize == -1) {
                // "All" option - use a large number
                pageSize = Integer.MAX_VALUE - 1;
            } else {
                pageSize = newSize;
            }
            pageNumber = 0;
            refreshData();
        });
        paginationBar.addComponentAsFirst(pageSizeSelector);
    }

    /**
     * Adds the floating toolbar for bulk actions.
     * Icon-based toolbar that appears when items are selected.
     */
    protected void addFloatingToolbar() {
        floatingToolbar = new BervanFloatingToolbar();
        floatingToolbar.setEditEnabled(true);
        floatingToolbar.setDeleteEnabled(true);
        floatingToolbar.setExportEnabled(isExportable());
        floatingToolbar.setSingleSelectOnly(true);

        // Wire up standard actions
        floatingToolbar.addEditClickListener(e -> handleFloatingToolbarEdit());
        floatingToolbar.addDeleteClickListener(e -> handleFloatingToolbarDelete());
        floatingToolbar.addExportClickListener(e -> handleFloatingToolbarExport());
        floatingToolbar.addCloseClickListener(e -> clearSelection());

        add(floatingToolbar);
    }

    /**
     * Handles export action from floating toolbar.
     * Triggers the existing export dialog with password decryption support.
     */
    protected void handleFloatingToolbarExport() {
        if (tableToolbarActions != null) {
            // Trigger the export button click programmatically
            Set<String> selected = getSelectedItemsByCheckbox();
            if (selected.isEmpty()) {
                showErrorNotification("Please select items to export");
                return;
            }

            List<T> toExport = data.stream()
                    .filter(e -> e.getId() != null && selected.contains(e.getId().toString()))
                    .collect(Collectors.toList());

            // Open export dialog directly using the toolbar's method
            tableToolbarActions.openExportDialog(toExport, service, pathToFileStorage, globalTmpDir);
        }
    }

    /**
     * Returns the floating toolbar for subclasses to add custom actions.
     */
    protected BervanFloatingToolbar getFloatingToolbar() {
        return floatingToolbar;
    }

    /**
     * Sets up keyboard navigation shortcuts.
     */
    protected void setupKeyboardNavigation() {
        // Escape - clear selection
        ShortcutRegistration escapeShortcut = UI.getCurrent().addShortcutListener(
                () -> clearSelection(),
                Key.ESCAPE
        );

        // Delete - delete selected with confirmation
        ShortcutRegistration deleteShortcut = UI.getCurrent().addShortcutListener(
                () -> {
                    if (isAtLeastOneCheckboxSelected()) {
                        handleFloatingToolbarDelete();
                    }
                },
                Key.DELETE
        );

        // Ctrl+A - select all
        ShortcutRegistration selectAllShortcut = UI.getCurrent().addShortcutListener(
                () -> {
                    if (selectAllCheckbox != null) {
                        selectAllCheckbox.setValue(true);
                    }
                },
                Key.KEY_A,
                com.vaadin.flow.component.KeyModifier.CONTROL
        );
    }

    /**
     * Loads persisted state from localStorage.
     */
    protected void loadPersistedState() {
        if (tableState != null) {
            tableState.loadFromStorage(state -> {
                // Apply persisted page size
                if (tableConfig.isPageSizeSelectorEnabled() && state.getPageSize() > 0) {
                    pageSize = state.getPageSize();
                    if (pageSizeSelector != null) {
                        pageSizeSelector.setValue(pageSize);
                    }
                }

                // Apply persisted column visibility
                if (tableConfig.isColumnToggleEnabled()) {
                    Map<String, Boolean> visibility = state.getColumnVisibility();
                    visibility.forEach((key, visible) -> {
                        Grid.Column<T> column = columnMap.get(key);
                        if (column != null) {
                            column.setVisible(visible);
                        }
                    });
                }
            });
        }
    }

    /**
     * Handles edit action from floating toolbar.
     */
    protected void handleFloatingToolbarEdit() {
        Set<String> selected = getSelectedItemsByCheckbox();
        if (selected.size() != 1) {
            showErrorNotification("Please select exactly one item to edit");
            return;
        }

        T item = data.stream()
                .filter(e -> e.getId() != null && selected.contains(e.getId().toString()))
                .findFirst()
                .orElse(null);

        if (item != null && tableToolbarActions != null && tableToolbarActions.getEditItemDialog() != null) {
            Dialog dialog = new Dialog();
            dialog.setWidth("60vw");
            dialog.add(tableToolbarActions.getEditItemDialog().buildEditItemDialog(dialog, item));
            dialog.open();
        }
    }

    /**
     * Handles delete action from floating toolbar.
     */
    protected void handleFloatingToolbarDelete() {
        Set<String> selected = getSelectedItemsByCheckbox();
        if (selected.isEmpty()) {
            return;
        }

        List<T> toDelete = data.stream()
                .filter(e -> e.getId() != null && selected.contains(e.getId().toString()))
                .collect(Collectors.toList());

        com.vaadin.flow.component.confirmdialog.ConfirmDialog confirmDialog =
                new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        confirmDialog.setHeader("Confirm Deletion");
        confirmDialog.setText("Are you sure you want to delete " + toDelete.size() + " item(s)?");
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.setCancelable(true);

        confirmDialog.addConfirmListener(e -> {
            for (T item : toDelete) {
                service.deleteById(item.getId()); //for deleting original
            }
            refreshData();
            showSuccessNotification("Deleted " + toDelete.size() + " item(s)");
        });

        confirmDialog.open();
    }


    /**
     * Clears all selections and hides the floating toolbar.
     */
    protected void clearSelection() {
        if (selectAllCheckbox != null) {
            selectAllCheckbox.setValue(false);
        }
        for (Checkbox checkbox : checkboxes) {
            checkbox.setValue(false);
        }
        updateSelectedItemsLabel();
        hideFloatingToolbar();
    }

    /**
     * Hides the floating toolbar by setting count to 0.
     */
    protected void hideFloatingToolbar() {
        if (floatingToolbar != null) {
            floatingToolbar.setSelectedCount(0);
        }
    }

    /**
     * Updates the floating toolbar based on selection.
     */
    protected void updateFloatingToolbar() {
        if (floatingToolbar != null) {
            int count = (int) checkboxes.stream().filter(AbstractField::getValue).count();
            floatingToolbar.setSelectedCount(count);
        }
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
            ClassViewAutoConfigColumn config = buildColumnConfig(vaadinTableColumn, bervanViewConfig);
            String columnInternalName = bervanViewConfig.getInternalName(vaadinTableColumn);
            String columnName = config.getDisplayName();

            if (!config.isInTable()) {
                continue;
            }

            for (ColumnForGridBuilder columnGridBuilder : columnGridBuilders) {
                if (columnGridBuilder.supports(config.getExtension(), config, tClass)) {
                    Grid.Column<T> column = grid.addColumn(columnGridBuilder.build(vaadinTableColumn, config))
                            .setHeader(columnName)
                            .setKey(columnInternalName)
                            .setResizable(columnGridBuilder.isResizable())
                            .setSortable(columnGridBuilder.isSortable());
                    // Store column reference for visibility toggle
                    columnMap.put(columnInternalName, column);
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
            updateFloatingToolbar();
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
                        updateFloatingToolbar();

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
                log.error(buildContext(), "Could not create checkbox column in table!", e);
                showErrorNotification("Could not checkbox create column in table!");
            }
        };
    }

    public boolean isAtLeastOneCheckboxSelected() {
        return checkboxes.parallelStream().anyMatch(AbstractField::getValue);
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

    protected void buildToolbarActionBar() {
        BervanTableToolbar<ID, T> toolbar = new BervanTableToolbar<>(checkboxes, data, tClass, bervanViewConfig, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange,
                (Void v) -> {
                    refreshData();
                    return v;
                }, service);

        // Enable icon buttons when floating toolbar is enabled (modern UI)
        if (tableConfig.isFloatingToolbarEnabled()) {
            toolbar.withIconButtons();
        }

        tableToolbarActions = toolbar
                .withEditButton(service)
                .withDeleteButton()
                .withExportButton(isExportable(), service, pathToFileStorage, globalTmpDir)
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
        try {
            SearchRequest request = filtersLayout.buildCombinedFilters();

            if (sortDirection != null) {
                if (columnSorted != null) {
                    sortField = columnSorted.getKey();
                } else if (sortField == null) {
                    sortField = "id";
                }
                if (sortDirection != SortDirection.ASCENDING) {
                    this.sortDir = com.bervan.common.search.model.SortDirection.DESC;
                } else {
                    this.sortDir = com.bervan.common.search.model.SortDirection.ASC;
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

//            reloadItemsCountInfo();
//            updateCurrentPageText();

            return collect;

        } catch (Exception e) {
            log.error(buildContext(), "Could not load table!", e);
            throw new RuntimeException(e);
        }
    }

    protected void postSearchUpdate(List<T> collect) {

    }

    protected List<String> getFieldsToFetchForTable() {
        List<String> result = new ArrayList<>();
        for (Field vaadinTableColumn : getFetchableVaadinTableFields()) {
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

    /**
     * Hook for subclasses to add custom buttons to the top table actions bar.
     * Called after the Add button is added but before the toolbar action bar is built.
     */
    protected void customizeTopTableActions(HorizontalLayout topTableActions) {
        // Override in subclasses to add custom buttons
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

        if ("checkboxColumnKey".equals(clickedColumn)) {
            return;
        }

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
        for (T item : items) {
            service.deleteById(item.getId()); //for deleting original
        }
    }

    protected void removeItemFromGrid(T item) {
        int oldSize = data.size();
        data.remove(item);
        if (oldSize == data.size()) {
            ID id = item.getId();
            data.removeIf(e -> e.getId().equals(id));
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
            buttonsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

            buttonsLayout.add(dialogSaveButton);

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
                    T newObject = tClass.getConstructor().newInstance();
                    for (Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry : fieldsHolder.entrySet()) {
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(true);
                        fieldAutoConfigurableFieldEntry.getKey().set(newObject, componentHelper.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry));
                        fieldAutoConfigurableFieldEntry.getKey().setAccessible(false);
                    }

                    newObject = preSaveActions(newObject);

                    T save = save(newObject);

                    postSaveActions(save);
                } catch (Exception e) {
                    log.error(buildContext(), "Could not save new item!", e);
                    showErrorNotification("Could not save new item!");
                }
                dialog.close();
            });

            dialogLayout.add(headerLayout, formLayout, buttonsLayout);
        } catch (Exception e) {
            log.error(buildContext(), "Error during using creation modal. Check columns name or create custom modal!", e);
            showErrorNotification("Error during using creation modal. Check columns name or create custom modal!");
        }
    }

    protected void postSaveActions(T save) {
        super.postSaveActions(save);
        refreshData();
    }

    protected T preSaveActions(T newItem) {
        return newItem;
    }
}