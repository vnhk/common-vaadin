package com.bervan.common.view.table;

import com.vaadin.flow.data.provider.SortDirection;

import java.io.Serializable;
import java.util.*;

/**
 * Tracks multi-column sort state for BervanTable.
 * Supports Shift+click for adding secondary/tertiary sorts.
 */
public class MultiSortState implements Serializable {

    private final List<SortColumn> sortColumns = new ArrayList<>();
    private final int maxSortColumns;

    public static class SortColumn implements Serializable {
        private final String columnKey;
        private final SortDirection direction;
        private final int priority;

        public SortColumn(String columnKey, SortDirection direction, int priority) {
            this.columnKey = columnKey;
            this.direction = direction;
            this.priority = priority;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public SortDirection getDirection() {
            return direction;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SortColumn that = (SortColumn) o;
            return Objects.equals(columnKey, that.columnKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(columnKey);
        }
    }

    public MultiSortState() {
        this(3); // Default max of 3 sort columns
    }

    public MultiSortState(int maxSortColumns) {
        this.maxSortColumns = maxSortColumns;
    }

    /**
     * Handles a column click for sorting.
     *
     * @param columnKey The column being clicked
     * @param shiftKey  Whether Shift was held (for multi-sort)
     */
    public void handleColumnClick(String columnKey, boolean shiftKey) {
        if (shiftKey) {
            handleMultiSortClick(columnKey);
        } else {
            handleSingleSortClick(columnKey);
        }
    }

    private void handleSingleSortClick(String columnKey) {
        Optional<SortColumn> existing = findColumn(columnKey);

        if (existing.isPresent()) {
            SortColumn current = existing.get();
            sortColumns.clear();

            if (current.getDirection() == SortDirection.ASCENDING) {
                // Toggle to descending
                sortColumns.add(new SortColumn(columnKey, SortDirection.DESCENDING, 0));
            } else {
                // Was descending, remove sort (clear state)
                // sortColumns is already cleared
            }
        } else {
            // New column, start with ascending
            sortColumns.clear();
            sortColumns.add(new SortColumn(columnKey, SortDirection.ASCENDING, 0));
        }
    }

    private void handleMultiSortClick(String columnKey) {
        Optional<SortColumn> existing = findColumn(columnKey);

        if (existing.isPresent()) {
            SortColumn current = existing.get();
            int index = sortColumns.indexOf(current);

            if (current.getDirection() == SortDirection.ASCENDING) {
                // Toggle to descending
                sortColumns.set(index, new SortColumn(columnKey, SortDirection.DESCENDING, current.getPriority()));
            } else {
                // Remove this column from sort
                sortColumns.remove(index);
                reindexPriorities();
            }
        } else {
            // Add as new sort column if under max
            if (sortColumns.size() < maxSortColumns) {
                sortColumns.add(new SortColumn(columnKey, SortDirection.ASCENDING, sortColumns.size()));
            }
        }
    }

    private void reindexPriorities() {
        List<SortColumn> reindexed = new ArrayList<>();
        for (int i = 0; i < sortColumns.size(); i++) {
            SortColumn col = sortColumns.get(i);
            reindexed.add(new SortColumn(col.getColumnKey(), col.getDirection(), i));
        }
        sortColumns.clear();
        sortColumns.addAll(reindexed);
    }

    private Optional<SortColumn> findColumn(String columnKey) {
        return sortColumns.stream()
                .filter(sc -> sc.getColumnKey().equals(columnKey))
                .findFirst();
    }

    /**
     * Returns the current sort columns in priority order.
     */
    public List<SortColumn> getSortColumns() {
        return Collections.unmodifiableList(sortColumns);
    }

    /**
     * Returns the sort direction for a specific column, or null if not sorted.
     */
    public SortDirection getSortDirection(String columnKey) {
        return findColumn(columnKey)
                .map(SortColumn::getDirection)
                .orElse(null);
    }

    /**
     * Returns the sort priority for a specific column (0-based), or -1 if not sorted.
     */
    public int getSortPriority(String columnKey) {
        return findColumn(columnKey)
                .map(SortColumn::getPriority)
                .orElse(-1);
    }

    /**
     * Checks if multi-sort is active (more than one column sorted).
     */
    public boolean isMultiSortActive() {
        return sortColumns.size() > 1;
    }

    /**
     * Clears all sort state.
     */
    public void clear() {
        sortColumns.clear();
    }

    /**
     * Sets sort state from a list (used for restoring from persistence).
     */
    public void setSortColumns(List<SortColumn> columns) {
        sortColumns.clear();
        for (int i = 0; i < Math.min(columns.size(), maxSortColumns); i++) {
            sortColumns.add(columns.get(i));
        }
    }

    /**
     * Converts to BervanTableState.SortState list for persistence.
     */
    public List<BervanTableState.SortState> toSortStates() {
        List<BervanTableState.SortState> states = new ArrayList<>();
        for (SortColumn col : sortColumns) {
            states.add(new BervanTableState.SortState(
                col.getColumnKey(),
                col.getDirection() == SortDirection.ASCENDING,
                col.getPriority()
            ));
        }
        return states;
    }

    /**
     * Restores from BervanTableState.SortState list.
     */
    public void fromSortStates(List<BervanTableState.SortState> states) {
        sortColumns.clear();
        for (BervanTableState.SortState state : states) {
            if (sortColumns.size() < maxSortColumns) {
                sortColumns.add(new SortColumn(
                    state.getColumnKey(),
                    state.isAscending() ? SortDirection.ASCENDING : SortDirection.DESCENDING,
                    state.getPriority()
                ));
            }
        }
    }

    public int getMaxSortColumns() {
        return maxSortColumns;
    }
}
