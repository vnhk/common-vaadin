package com.bervan.common.view.table;

import java.io.Serializable;
import java.util.*;

/**
 * Configuration class for BervanTable modern features.
 * All features default to OFF for backward compatibility with existing views.
 *
 * Usage:
 * <pre>
 * BervanTableConfig config = BervanTableConfig.builder()
 *     .floatingToolbarEnabled(true)
 *     .glassmorphismEnabled(true)
 *     .columnToggleEnabled(true)
 *     .build();
 * </pre>
 */
public class BervanTableConfig implements Serializable {

    // Feature flags - all default to false for backward compatibility
    private final boolean floatingToolbarEnabled;
    private final boolean glassmorphismEnabled;
    private final boolean columnToggleEnabled;
    private final boolean multiColumnSortEnabled;
    private final boolean quickFiltersEnabled;
    private final boolean keyboardNavigationEnabled;
    private final boolean columnPinningEnabled;
    private final boolean dragDropReorderEnabled;
    private final boolean virtualScrollingEnabled;
    private final boolean pageSizeSelectorEnabled;

    // Export configuration
    private final Set<ExportFormat> exportFormats;

    // Page size options (e.g., 10, 25, 50, 100, -1 for "All")
    private final List<Integer> pageSizeOptions;

    // Default page size
    private final int defaultPageSize;

    // State persistence key prefix (for localStorage)
    private final String stateKeyPrefix;

    // Theme (dark/light)
    private final Theme theme;

    public enum ExportFormat {
        CSV, EXCEL, JSON
    }

    public enum Theme {
        DARK, LIGHT, SYSTEM
    }

    private BervanTableConfig(Builder builder) {
        this.floatingToolbarEnabled = builder.floatingToolbarEnabled;
        this.glassmorphismEnabled = builder.glassmorphismEnabled;
        this.columnToggleEnabled = builder.columnToggleEnabled;
        this.multiColumnSortEnabled = builder.multiColumnSortEnabled;
        this.quickFiltersEnabled = builder.quickFiltersEnabled;
        this.keyboardNavigationEnabled = builder.keyboardNavigationEnabled;
        this.columnPinningEnabled = builder.columnPinningEnabled;
        this.dragDropReorderEnabled = builder.dragDropReorderEnabled;
        this.virtualScrollingEnabled = builder.virtualScrollingEnabled;
        this.pageSizeSelectorEnabled = builder.pageSizeSelectorEnabled;
        this.exportFormats = Collections.unmodifiableSet(new HashSet<>(builder.exportFormats));
        this.pageSizeOptions = Collections.unmodifiableList(new ArrayList<>(builder.pageSizeOptions));
        this.defaultPageSize = builder.defaultPageSize;
        this.stateKeyPrefix = builder.stateKeyPrefix;
        this.theme = builder.theme;
    }

    /**
     * Returns the default configuration with all features disabled.
     */
    public static BervanTableConfig defaults() {
        return new Builder().build();
    }

    /**
     * Returns a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public boolean isFloatingToolbarEnabled() {
        return floatingToolbarEnabled;
    }

    public boolean isGlasmorphismEnabled() {
        return glassmorphismEnabled;
    }

    public boolean isColumnToggleEnabled() {
        return columnToggleEnabled;
    }

    public boolean isMultiColumnSortEnabled() {
        return multiColumnSortEnabled;
    }

    public boolean isQuickFiltersEnabled() {
        return quickFiltersEnabled;
    }

    public boolean isKeyboardNavigationEnabled() {
        return keyboardNavigationEnabled;
    }

    public boolean isColumnPinningEnabled() {
        return columnPinningEnabled;
    }

    public boolean isDragDropReorderEnabled() {
        return dragDropReorderEnabled;
    }

    public boolean isVirtualScrollingEnabled() {
        return virtualScrollingEnabled;
    }

    public boolean isPageSizeSelectorEnabled() {
        return pageSizeSelectorEnabled;
    }

    public Set<ExportFormat> getExportFormats() {
        return exportFormats;
    }

    public List<Integer> getPageSizeOptions() {
        return pageSizeOptions;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public String getStateKeyPrefix() {
        return stateKeyPrefix;
    }

    public Theme getTheme() {
        return theme;
    }

    /**
     * Checks if any modern feature is enabled.
     */
    public boolean hasAnyModernFeature() {
        return floatingToolbarEnabled || glassmorphismEnabled || columnToggleEnabled ||
               multiColumnSortEnabled || quickFiltersEnabled || keyboardNavigationEnabled ||
               columnPinningEnabled || dragDropReorderEnabled || virtualScrollingEnabled ||
               pageSizeSelectorEnabled || !exportFormats.isEmpty();
    }

    /**
     * Builder for BervanTableConfig.
     */
    public static class Builder {
        private boolean floatingToolbarEnabled = false;
        private boolean glassmorphismEnabled = false;
        private boolean columnToggleEnabled = false;
        private boolean multiColumnSortEnabled = false;
        private boolean quickFiltersEnabled = false;
        private boolean keyboardNavigationEnabled = false;
        private boolean columnPinningEnabled = false;
        private boolean dragDropReorderEnabled = false;
        private boolean virtualScrollingEnabled = false;
        private boolean pageSizeSelectorEnabled = false;
        private Set<ExportFormat> exportFormats = new HashSet<>();
        private List<Integer> pageSizeOptions = Arrays.asList(10, 25, 50, 100, -1);
        private int defaultPageSize = 50;
        private String stateKeyPrefix = "bervan-table";
        private Theme theme = Theme.DARK;

        public Builder floatingToolbarEnabled(boolean enabled) {
            this.floatingToolbarEnabled = enabled;
            return this;
        }

        public Builder glassmorphismEnabled(boolean enabled) {
            this.glassmorphismEnabled = enabled;
            return this;
        }

        public Builder columnToggleEnabled(boolean enabled) {
            this.columnToggleEnabled = enabled;
            return this;
        }

        public Builder multiColumnSortEnabled(boolean enabled) {
            this.multiColumnSortEnabled = enabled;
            return this;
        }

        public Builder quickFiltersEnabled(boolean enabled) {
            this.quickFiltersEnabled = enabled;
            return this;
        }

        public Builder keyboardNavigationEnabled(boolean enabled) {
            this.keyboardNavigationEnabled = enabled;
            return this;
        }

        public Builder columnPinningEnabled(boolean enabled) {
            this.columnPinningEnabled = enabled;
            return this;
        }

        public Builder dragDropReorderEnabled(boolean enabled) {
            this.dragDropReorderEnabled = enabled;
            return this;
        }

        public Builder virtualScrollingEnabled(boolean enabled) {
            this.virtualScrollingEnabled = enabled;
            return this;
        }

        public Builder pageSizeSelectorEnabled(boolean enabled) {
            this.pageSizeSelectorEnabled = enabled;
            return this;
        }

        public Builder exportFormats(Set<ExportFormat> formats) {
            this.exportFormats = new HashSet<>(formats);
            return this;
        }

        public Builder addExportFormat(ExportFormat format) {
            this.exportFormats.add(format);
            return this;
        }

        public Builder pageSizeOptions(List<Integer> options) {
            this.pageSizeOptions = new ArrayList<>(options);
            return this;
        }

        public Builder defaultPageSize(int size) {
            this.defaultPageSize = size;
            return this;
        }

        public Builder stateKeyPrefix(String prefix) {
            this.stateKeyPrefix = prefix;
            return this;
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return this;
        }

        /**
         * Enables all modern features at once.
         */
        public Builder enableAllModernFeatures() {
            this.floatingToolbarEnabled = true;
            this.glassmorphismEnabled = true;
            this.columnToggleEnabled = true;
            this.multiColumnSortEnabled = true;
            this.quickFiltersEnabled = true;
            this.keyboardNavigationEnabled = true;
            this.columnPinningEnabled = true;
            this.dragDropReorderEnabled = true;
            this.virtualScrollingEnabled = true;
            this.pageSizeSelectorEnabled = true;
            this.exportFormats = EnumSet.allOf(ExportFormat.class);
            return this;
        }

        public BervanTableConfig build() {
            return new BervanTableConfig(this);
        }
    }
}
