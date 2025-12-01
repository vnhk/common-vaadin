package com.bervan.common.service;


import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableData;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchResponse;
import com.bervan.common.search.model.SortDirection;
import com.bervan.history.model.BaseRepository;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.logging.JsonLogger;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.Entity;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class BaseService<ID extends Serializable, T extends PersistableData<ID>> {
    protected final SearchService searchService;
    private final JsonLogger log = JsonLogger.getLogger(getClass());
    private final Class<T> entityType;
    protected BaseRepository<T, ID> repository;

    protected BaseService(BaseRepository<T, ID> repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;

        this.entityType = (Class<T>) ((ParameterizedType) getGenericBaseServiceSuperclass()).getActualTypeArguments()[1];
    }

    private Type getGenericBaseServiceSuperclass() {
        Class aClass = getClass();
        Class genericSuperclass = null;
        do {
            genericSuperclass = aClass.getSuperclass();
            if (genericSuperclass == Object.class) {
                throw new RuntimeException("Class is not an instance of BaseService");
            }

            if (genericSuperclass == BaseService.class) {
                return aClass.getGenericSuperclass();
            }

            aClass = genericSuperclass;
        } while (true);
    }

    public void save(List<T> data) {
        for (T datum : data) {
            save(datum);
        }
    }

    public T save(T data) {
        return repository.save(data);
    }

    public Set<T> load(Pageable pageable) {
        SearchRequest result = buildLoadSearchRequestData(new SearchRequest());
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.setSortField("id");
        options.setPage(pageable.getPageNumber());
        options.setPageSize(pageable.getPageSize());
        options.isCountQuery(false);

        SearchResponse<T> search = searchService.search(result, options);
        return new HashSet<>(search.getResultList());
    }

    public List<T> load(SearchRequest request, Pageable pageable, String sortField, SortDirection sortDirection, List<String> columnsToFetch) {
        SearchRequest result = buildLoadSearchRequestData(request);
        result.merge(request);
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.setSortField(sortField);
        options.setColumnsToFetch(columnsToFetch);
        options.setSortDirection(sortDirection);
        options.setPage(pageable.getPageNumber());
        options.setPageSize(pageable.getPageSize());
        options.isCountQuery(false);
        SearchResponse<T> search = searchService.search(result, options);
        return search.getResultList();
    }

    public Set<T> load(SearchRequest request, Pageable pageable) {
        SearchRequest result = buildLoadSearchRequestData(request);
        result.merge(request);
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.setSortField("id");
        options.setPage(pageable.getPageNumber());
        options.setPageSize(pageable.getPageSize());
        options.isCountQuery(false);
        SearchResponse<T> search = searchService.search(result, options);
        return new HashSet<>(search.getResultList());
    }

    public Optional<T> loadById(ID id) {
        SearchRequest request = new SearchRequest();
        request.addIdEqualsCriteria("ID_GROUP", entityType, id);
        Set<T> result = load(request, Pageable.ofSize(1));

        if (result.size() != 1) {
            log.error("Element not found by id, expected 1 found: {}", result.size());
            return Optional.empty();
        } else {
            return Optional.of(result.iterator().next());
        }
    }

    public long loadCount(SearchRequest request) {
        SearchRequest result = buildLoadSearchRequestData(request);
        result.merge(request);
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.isCountQuery(true);

        SearchResponse<T> search = searchService.search(result, options);
        return search.getAllFound();
    }

    public long loadCount() {
        SearchRequest result = buildLoadSearchRequestData(new SearchRequest());
        SearchQueryOption options = new SearchQueryOption((Class<? extends BervanBaseEntity>) entityType);
        options.isCountQuery(true);

        SearchResponse<T> search = searchService.search(result, options);
        return search.getAllFound();
    }

    private SearchRequest buildLoadSearchRequestData(SearchRequest request) {
        SearchRequest searchRequest = new SearchRequest();

        if (!request.containsGroup(SearchRequest.OWNER_ACCESS_GROUP) && request.isAddOwnerCriterion()) {
            searchRequest.addOwnerAccessCriteria(entityType);
        }

        if (Arrays.stream(entityType.getDeclaredFields()).peek(e -> e.setAccessible(true)).anyMatch(e -> e.getName().equals("deleted"))) {
            searchRequest.addDeletedFalseCriteria(entityType);
        }

        return searchRequest;
    }

    public void delete(T item) {
        item.setDeleted(true);
        save(item);
    }

    public void deleteById(ID id) {
        delete(repository.findById(id).get());
    }

    @RolesAllowed("USER")
    @Transactional
    public void saveIfValid(List<? extends ExcelIEEntity<ID>> objects) {
        try {
            List<? extends ExcelIEEntity<ID>> list = objects.stream()
                    .filter(e -> entityType.isInstance(e))
                    .toList();
            log.debug("Filtered values to be imported: " + list.size());

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
                    } else {
                        repository.save(((T) excelIEEntity));
                    }
                } else {
                    repository.save(((T) excelIEEntity));
                }
            }
        } catch (Exception e) {
            log.error("Unable to perform safeIfValid", e);
            throw new RuntimeException("Unable to perform safeIfValid");
        }
        log.info("Import successful!");
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

    public T findById(ID id) {
        return repository.findById(id).orElse(null);
    }
}
