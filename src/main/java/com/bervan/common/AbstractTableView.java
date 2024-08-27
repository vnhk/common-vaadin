package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.service.BaseService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractTableView<T extends PersistableTableData> extends AbstractPageView implements AfterNavigationObserver {
    protected final Set<T> data = new HashSet<>();
    protected final BaseService<T> service;
    protected Grid<T> grid;
    protected AbstractPageLayout pageLayout;
    private final String pageName;
    protected Button addButton;
    protected final VerticalLayout contentLayout = new VerticalLayout();
    private final Set<String> currentlySortedColumns = new HashSet<>();
    protected H3 header;

    public AbstractTableView(AbstractPageLayout pageLayout, @Autowired BaseService<T> service, String pageName) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.pageName = pageName;

        addClassName("bervan-table-view");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        data.addAll(loadData());
    }

    public void renderCommonComponents() {
        header = new H3(pageName);
        grid = getGrid();
        grid.setItems(data);
        grid.addItemClickListener(this::doOnColumnClick);
        grid.getColumns().forEach(column -> column.setClassNameGenerator(item -> "top-aligned-cell"));

        TextField searchField = getFilter();

        addButton = new Button("Add New Element", e -> newItemButtonClick());
        addButton.addClassName("option-button");

        contentLayout.add(header, searchField, grid, addButton);
        add(pageLayout);
        add(contentLayout);
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
        return this.service.load();
    }

    protected void refreshData() {
        this.data.removeAll(this.data);
        this.data.addAll(this.service.load());
    }

    protected void filterTable(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            grid.setItems(data);
        } else {
            List<T> collect = data.stream()
                    .filter(q -> q.getName().toLowerCase().contains(filterText.toLowerCase()))
                    .collect(Collectors.toList());
            grid.setItems(collect);
        }
    }

    protected TextField getFilter() {
        TextField searchField = new TextField("Filter table...");
        searchField.setWidth("100%");
        searchField.addValueChangeListener(e -> filterTable(e.getValue()));
        return searchField;
    }

    protected HorizontalLayout getDialogTopBarLayout(Dialog dialog) {
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addClassName("option-button");

        closeButton.addClickListener(e -> dialog.close());
        HorizontalLayout headerLayout = new HorizontalLayout(closeButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return headerLayout;
    }

    protected abstract Grid<T> getGrid();

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

    protected abstract void buildOnColumnClickDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout, String clickedColumn, T item);

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
        dialog.setWidth("80vw");
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);

        buildNewItemDialogContent(dialog, dialogLayout, headerLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    protected abstract void buildNewItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout);

}
