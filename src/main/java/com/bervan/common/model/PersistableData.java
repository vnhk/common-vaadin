package com.bervan.common.model;

import com.bervan.common.user.User;
import com.bervan.history.model.Persistable;

import java.util.Set;
import java.util.UUID;

public interface PersistableData<ID> extends Persistable<ID> {
    ID getId();

    default Boolean isDeleted() {
        return false;
    }

    Set<User> getOwners();

    void addOwner(User user);

    void removeOwner(User user);

    boolean hasAccess(User user);

    boolean hasAccess(UUID loggedUserId);
}
