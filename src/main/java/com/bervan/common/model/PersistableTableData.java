package com.bervan.common.model;

import java.util.UUID;

public interface PersistableTableData {
    String getTableFilterableColumnValue();

    UUID getId();
}
