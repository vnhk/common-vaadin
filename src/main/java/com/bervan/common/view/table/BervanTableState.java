package com.bervan.common.view.table;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Manages state persistence for BervanTable using browser localStorage.
 * Stores page size, column visibility, column order, and sort state.
 */
public class BervanTableState implements Serializable {

    private final String tableId;
    private final String storageKeyPrefix;

    private int pageSize = 50;
    private List<String> columnOrder = new ArrayList<>();
    private Map<String, Boolean> columnVisibility = new HashMap<>();
    private List<SortState> sortStates = new ArrayList<>();
    private Map<String, PinPosition> pinnedColumns = new HashMap<>();

    public enum PinPosition {
        LEFT, RIGHT, NONE
    }

    public static class SortState implements Serializable {
        private final String columnKey;
        private final boolean ascending;
        private final int priority;

        public SortState(String columnKey, boolean ascending, int priority) {
            this.columnKey = columnKey;
            this.ascending = ascending;
            this.priority = priority;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public boolean isAscending() {
            return ascending;
        }

        public int getPriority() {
            return priority;
        }
    }

    public BervanTableState(String tableId, String storageKeyPrefix) {
        this.tableId = tableId;
        this.storageKeyPrefix = storageKeyPrefix != null ? storageKeyPrefix : "bervan-table";
    }

    private String getStorageKey() {
        return storageKeyPrefix + "-" + tableId;
    }

    /**
     * Loads state from localStorage asynchronously.
     */
    public void loadFromStorage(Consumer<BervanTableState> callback) {
        UI ui = UI.getCurrent();
        if (ui == null) {
            callback.accept(this);
            return;
        }

        String storageKey = getStorageKey();
        ui.getPage().executeJs(
            "return localStorage.getItem($0)",
            storageKey
        ).then(String.class, jsonStr -> {
            if (jsonStr != null && !jsonStr.isEmpty()) {
                parseFromJson(jsonStr);
            }
            callback.accept(this);
        });
    }

    /**
     * Saves current state to localStorage.
     */
    public void saveToStorage() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }

        String storageKey = getStorageKey();
        String jsonStr = toJson();

        ui.getPage().executeJs(
            "localStorage.setItem($0, $1)",
            storageKey, jsonStr
        );
    }

    /**
     * Clears state from localStorage.
     */
    public void clearStorage() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }

        String storageKey = getStorageKey();
        ui.getPage().executeJs(
            "localStorage.removeItem($0)",
            storageKey
        );
    }

    private String toJson() {
        JsonObject json = Json.createObject();

        json.put("pageSize", pageSize);

        JsonArray orderArray = Json.createArray();
        for (int i = 0; i < columnOrder.size(); i++) {
            orderArray.set(i, columnOrder.get(i));
        }
        json.put("columnOrder", orderArray);

        JsonObject visibilityObj = Json.createObject();
        for (Map.Entry<String, Boolean> entry : columnVisibility.entrySet()) {
            visibilityObj.put(entry.getKey(), entry.getValue());
        }
        json.put("columnVisibility", visibilityObj);

        JsonArray sortArray = Json.createArray();
        for (int i = 0; i < sortStates.size(); i++) {
            SortState state = sortStates.get(i);
            JsonObject sortObj = Json.createObject();
            sortObj.put("columnKey", state.getColumnKey());
            sortObj.put("ascending", state.isAscending());
            sortObj.put("priority", state.getPriority());
            sortArray.set(i, sortObj);
        }
        json.put("sortStates", sortArray);

        JsonObject pinnedObj = Json.createObject();
        for (Map.Entry<String, PinPosition> entry : pinnedColumns.entrySet()) {
            pinnedObj.put(entry.getKey(), entry.getValue().name());
        }
        json.put("pinnedColumns", pinnedObj);

        return json.toJson();
    }

    private void parseFromJson(String jsonStr) {
        try {
            JsonObject json = Json.parse(jsonStr);

            if (json.hasKey("pageSize")) {
                this.pageSize = (int) json.getNumber("pageSize");
            }

            if (json.hasKey("columnOrder")) {
                JsonArray orderArray = json.getArray("columnOrder");
                this.columnOrder = new ArrayList<>();
                for (int i = 0; i < orderArray.length(); i++) {
                    this.columnOrder.add(orderArray.getString(i));
                }
            }

            if (json.hasKey("columnVisibility")) {
                JsonObject visibilityObj = json.getObject("columnVisibility");
                this.columnVisibility = new HashMap<>();
                for (String key : visibilityObj.keys()) {
                    this.columnVisibility.put(key, visibilityObj.getBoolean(key));
                }
            }

            if (json.hasKey("sortStates")) {
                JsonArray sortArray = json.getArray("sortStates");
                this.sortStates = new ArrayList<>();
                for (int i = 0; i < sortArray.length(); i++) {
                    JsonObject sortObj = sortArray.getObject(i);
                    this.sortStates.add(new SortState(
                        sortObj.getString("columnKey"),
                        sortObj.getBoolean("ascending"),
                        (int) sortObj.getNumber("priority")
                    ));
                }
            }

            if (json.hasKey("pinnedColumns")) {
                JsonObject pinnedObj = json.getObject("pinnedColumns");
                this.pinnedColumns = new HashMap<>();
                for (String key : pinnedObj.keys()) {
                    this.pinnedColumns.put(key, PinPosition.valueOf(pinnedObj.getString(key)));
                }
            }
        } catch (Exception e) {
            // If parsing fails, keep defaults
        }
    }

    // Getters and setters
    public String getTableId() {
        return tableId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<String> getColumnOrder() {
        return new ArrayList<>(columnOrder);
    }

    public void setColumnOrder(List<String> columnOrder) {
        this.columnOrder = new ArrayList<>(columnOrder);
    }

    public Map<String, Boolean> getColumnVisibility() {
        return new HashMap<>(columnVisibility);
    }

    public void setColumnVisibility(Map<String, Boolean> columnVisibility) {
        this.columnVisibility = new HashMap<>(columnVisibility);
    }

    public boolean isColumnVisible(String columnKey) {
        return columnVisibility.getOrDefault(columnKey, true);
    }

    public void setColumnVisible(String columnKey, boolean visible) {
        this.columnVisibility.put(columnKey, visible);
    }

    public List<SortState> getSortStates() {
        return new ArrayList<>(sortStates);
    }

    public void setSortStates(List<SortState> sortStates) {
        this.sortStates = new ArrayList<>(sortStates);
    }

    public void addSortState(String columnKey, boolean ascending) {
        int priority = sortStates.size();
        sortStates.add(new SortState(columnKey, ascending, priority));
    }

    public void clearSortStates() {
        sortStates.clear();
    }

    public Map<String, PinPosition> getPinnedColumns() {
        return new HashMap<>(pinnedColumns);
    }

    public void setPinnedColumns(Map<String, PinPosition> pinnedColumns) {
        this.pinnedColumns = new HashMap<>(pinnedColumns);
    }

    public PinPosition getColumnPinPosition(String columnKey) {
        return pinnedColumns.getOrDefault(columnKey, PinPosition.NONE);
    }

    public void pinColumn(String columnKey, PinPosition position) {
        if (position == PinPosition.NONE) {
            pinnedColumns.remove(columnKey);
        } else {
            pinnedColumns.put(columnKey, position);
        }
    }
}
