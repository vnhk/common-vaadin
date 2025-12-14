package com.bervan.common.model;

import com.bervan.history.model.Persistable;

public interface PersistableData<ID> extends Persistable<ID> {
    ID getId();

    default Boolean isDeleted() {
        return false;
    }

    void setDeleted(Boolean value);
}
