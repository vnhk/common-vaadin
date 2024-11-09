package com.bervan.common.model;

import java.util.UUID;

public interface PersistableData {
    UUID getId();

    default Boolean getDeleted() {
        return false;
    }

    default PersistableData getOwner() {
        return null;
    }

    default void setOwner() {

    }
}
