package com.bervan.common.view.table;

import java.io.Serializable;

/**
 * Enhanced column configuration for BervanTable modern features.
 * Extends beyond basic column settings to support pinning, visibility, sorting, and filtering.
 */
public class ColumnConfig implements Serializable {

    private final String key;
    private final String headerText;
    private boolean visible;
    private boolean sortable;
    private boolean filterable;
    private boolean resizable;
    private int width;
    private String minWidth;
    private String maxWidth;
    private BervanTableState.PinPosition pinPosition;
    private int order;
    private boolean frozen;
    private String filterType;

    public enum FilterType {
        TEXT,
        NUMBER,
        DATE,
        BOOLEAN,
        SELECT,
        MULTI_SELECT
    }

    private ColumnConfig(Builder builder) {
        this.key = builder.key;
        this.headerText = builder.headerText;
        this.visible = builder.visible;
        this.sortable = builder.sortable;
        this.filterable = builder.filterable;
        this.resizable = builder.resizable;
        this.width = builder.width;
        this.minWidth = builder.minWidth;
        this.maxWidth = builder.maxWidth;
        this.pinPosition = builder.pinPosition;
        this.order = builder.order;
        this.frozen = builder.frozen;
        this.filterType = builder.filterType;
    }

    public static Builder builder(String key) {
        return new Builder(key);
    }

    // Getters
    public String getKey() {
        return key;
    }

    public String getHeaderText() {
        return headerText;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public String getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
    }

    public BervanTableState.PinPosition getPinPosition() {
        return pinPosition;
    }

    public void setPinPosition(BervanTableState.PinPosition pinPosition) {
        this.pinPosition = pinPosition;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public boolean isPinned() {
        return pinPosition != null && pinPosition != BervanTableState.PinPosition.NONE;
    }

    public static class Builder {
        private final String key;
        private String headerText;
        private boolean visible = true;
        private boolean sortable = true;
        private boolean filterable = false;
        private boolean resizable = true;
        private int width = -1;
        private String minWidth = null;
        private String maxWidth = null;
        private BervanTableState.PinPosition pinPosition = BervanTableState.PinPosition.NONE;
        private int order = 0;
        private boolean frozen = false;
        private String filterType = "TEXT";

        public Builder(String key) {
            this.key = key;
            this.headerText = key;
        }

        public Builder headerText(String headerText) {
            this.headerText = headerText;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder sortable(boolean sortable) {
            this.sortable = sortable;
            return this;
        }

        public Builder filterable(boolean filterable) {
            this.filterable = filterable;
            return this;
        }

        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder minWidth(String minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public Builder maxWidth(String maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder pinPosition(BervanTableState.PinPosition pinPosition) {
            this.pinPosition = pinPosition;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder frozen(boolean frozen) {
            this.frozen = frozen;
            return this;
        }

        public Builder filterType(String filterType) {
            this.filterType = filterType;
            return this;
        }

        public ColumnConfig build() {
            return new ColumnConfig(this);
        }
    }
}
