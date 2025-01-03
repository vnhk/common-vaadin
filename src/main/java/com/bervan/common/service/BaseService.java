package com.bervan.common.service;


import com.bervan.common.model.PersistableData;
import com.bervan.common.search.SearchService;
import com.bervan.history.model.BaseRepository;
import com.bervan.ieentities.ExcelIEEntity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseService<ID extends Serializable, T extends PersistableData<ID>> {
    private final Logger logger = LoggerFactory.getLogger(BaseService.class);
    private final Class<T> entityType;

    protected BaseRepository<T, ID> repository;
    protected final SearchService searchService;

    protected BaseService(BaseRepository<T, ID> repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;

        this.entityType = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[1];
    }

    public abstract void save(List<T> data);

    public abstract T save(T data);

    public abstract Set<T> load();

    public abstract void delete(T item);

    @RolesAllowed("USER")
    @Transactional
    public void saveIfValid(List<? extends ExcelIEEntity<ID>> objects) {
        try {


            List<? extends ExcelIEEntity<ID>> list = objects.stream()
                    .filter(e -> entityType.isInstance(e))
                    .toList();
            logger.debug("Filtered values to be imported: " + list.size());

            for (ExcelIEEntity<ID> excelIEEntity : list) {
                if (excelIEEntity.getId() != null) {
                    Optional<T> byId = repository.findById(excelIEEntity.getId());
                    if (byId.isPresent()) {
                        T inDbItem = byId.get();
                        if (AuthService.hasAccess(inDbItem.getOwners())) {
                            checkDifferencesAndUpdate(inDbItem, (T) excelIEEntity);
                        } else {
                            throw new RuntimeException("User is trying to update item that does not belong to him! ID=" + inDbItem.getId());
                        }
                    }
                } else {
                    repository.save(((T) excelIEEntity));
                }
            }
        } catch (Exception e) {
            logger.error("Unable to perform safeIfValid", e);
            throw new RuntimeException("Unable to perform safeIfValid");
        }
    }

    private void checkDifferencesAndUpdate(T inDbItem, T newItem) throws IllegalAccessException {
        List<Field> declaredFields = getNonEntityFields(inDbItem.getClass());
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (!Objects.equals(declaredField.get(inDbItem), declaredField.get(newItem))) {
                declaredField.set(inDbItem, declaredField.get(newItem));
            }
            declaredField.setAccessible(false);
        }
    }

    private List<Field> getNonEntityFields(Class<?> clazz) {
        List<Field> nonEntityFields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!isEntityClass(field.getType())) {
                nonEntityFields.add(field);
            }
        }

        return nonEntityFields;
    }

    private boolean isEntityClass(Class<?> type) {
        return type.isAnnotationPresent(Entity.class);
    }

}
