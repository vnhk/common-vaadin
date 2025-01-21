package com.bervan.common.service;


import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchResponse;
import com.bervan.history.model.BaseRepository;
import com.bervan.ieentities.ExcelIEEntity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseService<ID extends Serializable, T extends PersistableTableData<ID>> {
    private final Logger logger = LoggerFactory.getLogger(BaseService.class);
    private final Class<T> entityType;

    protected BaseRepository<T, ID> repository;
    protected final SearchService searchService;

    protected BaseService(BaseRepository<T, ID> repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;

        this.entityType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    public abstract void save(List<T> data);

    public abstract T save(T data);

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public Set<T> load(Pageable pageable) {
        SearchRequest result = buildLoadSearchRequestData();
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.setSortField("id");
        options.setPage(pageable.getPageNumber());
        options.setPageSize(pageable.getPageSize());
        options.isCountQuery(false);

        SearchResponse<T> search = searchService.search(result, options);
        return new HashSet<>(search.getResultList());
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public Set<T> load(SearchRequest request, Pageable pageable) {
        SearchRequest result = buildLoadSearchRequestData();
        result.merge(request);
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.setSortField("id");
        options.setPage(pageable.getPageNumber());
        options.setPageSize(pageable.getPageSize());
        options.isCountQuery(false);
        SearchResponse<T> search = searchService.search(result, options);
        return new HashSet<>(search.getResultList());
    }

    public long loadCount(SearchRequest request) {
        SearchRequest result = buildLoadSearchRequestData();
        result.merge(request);
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.isCountQuery(true);

        SearchResponse<T> search = searchService.search(result, options);
        return search.getAllFound();
    }

    public long loadCount() {
        SearchRequest result = buildLoadSearchRequestData();
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.isCountQuery(true);

        SearchResponse<T> search = searchService.search(result, options);
        return search.getAllFound();
    }

    private SearchRequest buildLoadSearchRequestData() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.addOwnerAccessCriteria("G1", entityType);
        if (Arrays.stream(entityType.getDeclaredFields()).peek(e -> e.setAccessible(true)).anyMatch(e -> e.getName().equals("deleted"))) {
            searchRequest.addDeletedFalseCriteria("G1", entityType);
        }

        return searchRequest;
    }

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
                            repository.save(inDbItem);
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
