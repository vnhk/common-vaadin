package com.bervan.common;

import com.bervan.common.model.BervanHistoryEntity;
import com.bervan.common.model.PersistableData;
import com.bervan.common.user.User;
import com.bervan.history.model.AbstractBaseHistoryEntity;
import com.bervan.history.model.BaseRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BervanBaseRepositoryImpl<T extends PersistableData<ID>, ID extends Serializable> extends BaseRepositoryImpl<T, ID> {
    public BervanBaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public @NotNull Optional<T> findById(@NotNull ID id) {
        return super.findById(id);
    }

    @Override
    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public @NotNull List<T> findAll() {
        return super.findAll();
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public <S extends T> @NotNull S save(S entity) {
        if (!(entity instanceof User) && (entity.getOwners() == null || entity.getOwners().size() == 0)) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.addOwner(user);
        }

        trimStringValues(entity);

        if (entity.getId() == null) {
            try {
                entity.setId((ID) UUID.randomUUID());
            } catch (Exception ignored) {

            }
        }

        return super.save(entity);
    }

    private <S extends T> void trimStringValues(S entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(entity);
                    if (value != null) {
                        field.set(entity, value.trim()); // Trim the value and set it back
                    }
                } catch (IllegalAccessException e) {

                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public <S extends T> S saveWithoutHistory(S entity) {
        if (entity.getOwners() == null || entity.getOwners().size() == 0) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.addOwner(user);
        }
        return super.saveWithoutHistory(entity);
    }


    @Override
    protected void historyPreHistorySave(AbstractBaseHistoryEntity<ID> history) {
        BervanHistoryEntity entity = (BervanHistoryEntity) history;

        if (entity.getOwners() == null || entity.getOwners().size() == 0) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            entity.addOwner(user);
        }

        if (entity.getId() == null) {
            try {
                entity.setId((ID) UUID.randomUUID());
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public void delete(T entity) {
        super.delete(entity);
    }
}
