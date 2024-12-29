package com.bervan.common;

import com.bervan.common.model.PersistableData;
import com.bervan.common.service.AuthService;
import com.bervan.common.user.User;
import com.bervan.history.model.BaseRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class BervanBaseRepositoryImpl<T extends PersistableData<ID>, ID extends Serializable> extends BaseRepositoryImpl<T, ID> {
    public BervanBaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public @NotNull Optional<T> findById(@NotNull ID id) {
        Optional<T> byId = super.findById(id);

        if (byId.isPresent() && byId.get() instanceof User) {
            return byId;
        }

        if (byId.isPresent() && byId.get().hasAccess(AuthService.getLoggedUserId())) {
            return byId;
        }

        return Optional.empty();
    }

    @Override
    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public @NotNull List<T> findAll() {
        return super.findAll();
    }

    @Override
    public <S extends T> @NotNull S save(S entity) {
        if (entity.getOwners() == null || entity.getOwners().size() == 0) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.addOwner(user);
        }

        return super.save(entity);
    }

    @Override
    public <S extends T> S saveWithoutHistory(S entity) {
        if (entity.getOwners() == null || entity.getOwners().size() == 0) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.addOwner(user);
        }
        return super.saveWithoutHistory(entity);
    }
}
