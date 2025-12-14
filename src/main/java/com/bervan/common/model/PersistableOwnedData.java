package com.bervan.common.model;

import com.bervan.common.user.User;

import java.util.Set;
import java.util.UUID;

public interface PersistableOwnedData<ID> extends PersistableData<ID> {

    Set<User> getOwners();

    void addOwner(User user);

    void removeOwner(User user);

    boolean hasAccess(User user);

    boolean hasAccess(UUID loggedUserId);

}
