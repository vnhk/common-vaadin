package com.bervan.common;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BervanLogger;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
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
    protected final BervanLogger log;

    public AbstractTableView(AbstractPageLayout pageLayout, @Autowired BaseService<T> service, String pageName, BervanLogger log) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.pageName = pageName;
        this.log = log;

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

    protected final void buildGridAutomatically(Grid<T> grid) {
        Class<T> beanType = grid.getBeanType();
        List<Field> vaadinTableColumns = getVaadinTableColumns(beanType);
        for (Field vaadinTableColumn : vaadinTableColumns) {
            String columnInternalName = vaadinTableColumn.getAnnotation(VaadinTableColumn.class).internalName();
            String columnName = vaadinTableColumn.getAnnotation(VaadinTableColumn.class).displayName();
            grid.addColumn(createTextColumnComponent(vaadinTableColumn)).setHeader(columnName).setKey(columnInternalName)
                    .setResizable(true);
        }

        grid.getElement().getStyle().set("--lumo-size-m", 100 + "px");
    }

    private SerializableBiConsumer<Span, T> textColumnUpdater(Field f) {
        return (span, record) -> {
            try {
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o != null) {
                    span.add(o.toString());
                }
                customizeTextColumnUpdater(span, record, f);
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                Notification.show("Could not create column in table!");
            }
        };
    }

    protected void customizeTextColumnUpdater(Span span, T record, Field f) {

    }

    private ComponentRenderer<Span, T> createTextColumnComponent(Field f) {
        return new ComponentRenderer<>(Span::new, textColumnUpdater(f));
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

    protected void buildOnColumnClickDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout, String clickedColumn, T item) {
        Field field = null;
        try {
            List<Field> declaredFields = getVaadinTableColumns(item.getClass());

            field = declaredFields.stream()
                    .filter(e -> e.getAnnotation(VaadinTableColumn.class).internalName().equals(clickedColumn))
                    .findFirst().get();
            field.setAccessible(true);
            Object value = field.get(item);

            VerticalLayout layoutForField = new VerticalLayout();
            AbstractField componentWithValue = null;
            String typeName = field.getType().getTypeName();
            String displayName = field.getAnnotation(VaadinTableColumn.class).displayName();
            if (String.class.getTypeName().equals(typeName)) {
                componentWithValue = buildTextArea(value, clickedColumn, displayName);
            } else if (Integer.class.getTypeName().equals(typeName)) {
                componentWithValue = buildIntegerInput(value, clickedColumn, displayName);
            } else if (LocalDateTime.class.getTypeName().equals(typeName)) {
                componentWithValue = buildDateTimeInput(value, clickedColumn, displayName);
            } else {
                componentWithValue = new TextField("Not supported yet");
                componentWithValue.setValue(value);
            }

            layoutForField.add(componentWithValue);

            customFieldLayout(layoutForField, componentWithValue, clickedColumn, item);

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
            AbstractField finalComponentWithValue = componentWithValue;
            dialogSaveButton.addClickListener(buttonClickEvent -> {
                try {
                    finalField.setAccessible(true);
                    finalField.set(item, finalComponentWithValue.getValue());
                    finalField.setAccessible(false);

                    customizeSaving(finalField, layoutForField, clickedColumn, item);

                    service.save(item);

                    refreshDataAfterUpdate();
                } catch (IllegalAccessException e) {
                    log.error("Could not update field value!", e);
                    Notification.show("Could not update value!");
                }
                dialog.close();
            });

            dialogLayout.add(headerLayout, layoutForField, buttonsLayout);
        } catch (Exception e) {
            log.error("Error during using edit modal. Check columns name or create custom modal!", e);
            Notification.show("Error during using edit modal. Check columns name or create custom modal!");
        } finally {
            if (field != null) {
                field.setAccessible(false);
            }
        }
    }

    protected void modalDeleteItem(Dialog dialog, T item) {
        service.delete(item);
        this.data.remove(item);
        this.grid.getDataProvider().refreshAll();
        dialog.close();
    }

    protected void refreshDataAfterUpdate() {
        this.data.removeAll(this.data);
        this.data.addAll(this.service.load());
        this.grid.getDataProvider().refreshAll();
    }

    private List<Field> getVaadinTableColumns(Class<?> tClass) {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(VaadinTableColumn.class))
                .toList();
    }

    protected void customizeSaving(Field field, VerticalLayout layoutForField, String clickedColumn, T item) {

    }

    protected void customFieldLayout(VerticalLayout layoutForField, AbstractField componentWithValue, String clickedColumn, T item) {

    }

    private AbstractField buildDateTimeInput(Object value, String clickedColumn, String displayName) {
        DateTimePicker dateTimePicker = new DateTimePicker(displayName);
        dateTimePicker.setLabel("Select Date and Time");

        dateTimePicker.setValue((LocalDateTime) value);
        return dateTimePicker;
    }

    private AbstractField buildIntegerInput(Object value, String clickedColumn, String displayName) {
        IntegerField field = new IntegerField(displayName);
        field.setWidth("100%");
        field.setValue(((Integer) value));
        return field;
    }

    private AbstractField buildTextArea(Object value, String clickedColumn, String displayName) {
        TextArea textArea = new TextArea(displayName);
        textArea.setWidth("100%");
        textArea.setValue(((String) value));
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
        dialog.setWidth("80vw");
        VerticalLayout dialogLayout = new VerticalLayout();
        HorizontalLayout headerLayout = getDialogTopBarLayout(dialog);

        buildNewItemDialogContent(dialog, dialogLayout, headerLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    protected abstract void buildNewItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout);


}
