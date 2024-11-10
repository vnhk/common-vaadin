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
    public Optional<T> findById(ID id) {
        Optional<T> byId = super.findById(id);
        if (byId.isPresent() && byId.get().getOwner() != null) {
            if (AuthService.getLoggedUserId().equals(byId.get().getOwner().getId())) {
                return byId;
            }
        }
        return Optional.empty();
    }

    @Override
    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
    public @NotNull List<T> findAll() {
        return super.findAll();
    }

    @Override
    public <S extends T> @NotNull S save(S entity) {
        if (entity.getOwner() == null) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.setOwner(user);
        }

        return super.save(entity);
    }

    @Override
    public <S extends T> S saveWithoutHistory(S entity) {
        if (entity.getOwner() == null) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.setOwner(user);
        }
        return super.saveWithoutHistory(entity);
    }
}
