package com.bervan.common.model;

public interface PersistableTableData<ID> extends PersistableData<ID> {
    String getTableFilterableColumnValue();
}
