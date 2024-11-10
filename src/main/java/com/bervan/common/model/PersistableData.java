package com.bervan.common.model;

import com.bervan.common.user.User;
import com.bervan.history.model.Persistable;

public interface PersistableData<ID> extends Persistable<ID> {
    ID getId();

    default Boolean getDeleted() {
        return false;
    }

    User getOwner();

    void setOwner(User user);
}
