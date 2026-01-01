package com.bervan.common;

import com.bervan.common.model.BervanHistoryOwnedEntity;
import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.model.PersistableData;
import com.bervan.common.user.User;
import com.bervan.history.model.AbstractBaseHistoryEntity;
import com.bervan.history.model.BaseRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
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
        if (entity instanceof BervanOwnedBaseEntity<?> ownedBaseEntity) {
            if (!(ownedBaseEntity instanceof User) && (ownedBaseEntity.getOwners() == null || ownedBaseEntity.getOwners().isEmpty())) {
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                ownedBaseEntity.addOwner(user);
            }
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
        if (entity instanceof BervanOwnedBaseEntity<?> ownedBaseEntity) {
            if (ownedBaseEntity.getOwners() == null || ownedBaseEntity.getOwners().isEmpty()) {
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                ownedBaseEntity.addOwner(user);
            }
        }
        return super.saveWithoutHistory(entity);
    }


    @Override
    protected void historyPreHistorySave(AbstractBaseHistoryEntity<ID> history) {
        BervanHistoryOwnedEntity entity = (BervanHistoryOwnedEntity) history;
        BervanOwnedBaseEntity<ID> ownerEntity = (BervanOwnedBaseEntity<ID>) history.getEntity();
        Optional<? extends AbstractBaseHistoryEntity<ID>> lastHistory = ownerEntity.getHistoryEntities().stream().filter(e -> e instanceof BervanHistoryOwnedEntity)
                .max(Comparator.comparing((AbstractBaseHistoryEntity<ID> e) -> e.getUpdateDate()));

        if (entity.getOwners() == null || entity.getOwners().isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null && entity.getOwners() != null && entity.getOwners().size() == 1) {
                entity.addOwner(ownerEntity.getOwners().iterator().next());
            } else if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                entity.addOwner(user);
            } else if (lastHistory.isPresent()) {
                BervanOwnedBaseEntity<ID> last = (BervanOwnedBaseEntity<ID>) lastHistory.get();
                last.getOwners().forEach(entity::addOwner);
            }
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
