package com.bervan.common.model;

import com.bervan.common.user.User;
import com.bervan.history.model.AbstractBaseHistoryEntity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@MappedSuperclass
public abstract class BervanHistoryEntity<ID extends Serializable> implements AbstractBaseHistoryEntity<ID>, PersistableData<ID> {
    @ManyToMany
    protected Set<User> owners = new HashSet<>();

    @Override
    public Set<User> getOwners() {
        return new HashSet<>(owners);
    }

    @Override
    public void addOwner(User user) {
        owners.add(user);
    }

    @Override
    public void removeOwner(User user) {
        owners.remove(user);
    }

    @Override
    public boolean hasAccess(User user) {
        return owners.contains(user);
    }

    @Override
    public boolean hasAccess(UUID loggedUserId) {
        return owners.stream().anyMatch(e -> e.getId().equals(loggedUserId));
    }

    @Override
    public void setDeleted(Boolean value) {

    }
}
