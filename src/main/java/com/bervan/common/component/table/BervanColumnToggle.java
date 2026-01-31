package com.bervan.common.component.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.*;

/**
 * Java wrapper for the bervan-column-toggle Lit component.
 * Provides a dropdown for toggling column visibility.
 */
@Tag("bervan-column-toggle")
@JsModule("./src/bervan-table/bervan-column-toggle.ts")
public class BervanColumnToggle extends Component implements HasSize, HasStyle {

    public static class ColumnInfo {
        private final String key;
        private final String header;
        private boolean visible;

        public ColumnInfo(String key, String header, boolean visible) {
            this.key = key;
            this.header = header;
            this.visible = visible;
        }

        public String getKey() {
            return key;
        }

        public String getHeader() {
            return header;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }

    private List<ColumnInfo> columns = new ArrayList<>();

    /**
     * Creates a new column toggle.
     */
    public BervanColumnToggle() {
    }

    /**
     * Creates a new column toggle with specified columns.
     */
    public BervanColumnToggle(List<ColumnInfo> columns) {
        setColumns(columns);
    }

    /**
     * Sets the columns that can be toggled.
     */
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = new ArrayList<>(columns);
        updateColumnsProperty();
    }

    /**
     * Gets the current column configurations.
     */
    public List<ColumnInfo> getColumns() {
        return new ArrayList<>(columns);
    }

    /**
     * Adds a column to the toggle list.
     */
    public void addColumn(String key, String header, boolean visible) {
        columns.add(new ColumnInfo(key, header, visible));
        updateColumnsProperty();
    }

    /**
     * Sets the visibility of a specific column.
     */
    public void setColumnVisible(String key, boolean visible) {
        for (ColumnInfo col : columns) {
            if (col.getKey().equals(key)) {
                col.setVisible(visible);
                break;
            }
        }
        updateColumnsProperty();
    }

    /**
     * Gets whether a specific column is visible.
     */
    public boolean isColumnVisible(String key) {
        return columns.stream()
                .filter(col -> col.getKey().equals(key))
                .findFirst()
                .map(ColumnInfo::isVisible)
                .orElse(true);
    }

    /**
     * Sets all columns visible or hidden.
     */
    public void setAllColumnsVisible(boolean visible) {
        for (ColumnInfo col : columns) {
            col.setVisible(visible);
        }
        updateColumnsProperty();
    }

    /**
     * Sets the localStorage key for persisting column visibility.
     */
    public void setStorageKey(String key) {
        getElement().setProperty("storageKey", key);
    }

    /**
     * Sets the theme (dark/light).
     */
    public void setTheme(String theme) {
        getElement().setAttribute("theme", theme);
    }

    private void updateColumnsProperty() {
        JsonArray jsonArray = Json.createArray();
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo col = columns.get(i);
            JsonObject obj = Json.createObject();
            obj.put("key", col.getKey());
            obj.put("header", col.getHeader());
            obj.put("visible", col.isVisible());
            jsonArray.set(i, obj);
        }
        getElement().setPropertyJson("columns", jsonArray);
    }

    /**
     * Adds a listener for column visibility changes.
     */
    public void addColumnVisibilityChangeListener(
            ComponentEventListener<ColumnVisibilityChangeEvent> listener) {
        addListener(ColumnVisibilityChangeEvent.class, listener);
    }

    @DomEvent("column-visibility-change")
    public static class ColumnVisibilityChangeEvent extends ComponentEvent<BervanColumnToggle> {
        private final Map<String, Boolean> columnVisibility;

        public ColumnVisibilityChangeEvent(BervanColumnToggle source, boolean fromClient,
                                           @EventData("event.detail.columns") elemental.json.JsonArray columns) {
            super(source, fromClient);
            this.columnVisibility = new HashMap<>();

            if (columns != null) {
                for (int i = 0; i < columns.length(); i++) {
                    JsonObject col = columns.getObject(i);
                    columnVisibility.put(col.getString("key"), col.getBoolean("visible"));
                }
            }
        }

        public Map<String, Boolean> getColumnVisibility() {
            return columnVisibility;
        }

        public boolean isColumnVisible(String key) {
            return columnVisibility.getOrDefault(key, true);
        }
    }
}
