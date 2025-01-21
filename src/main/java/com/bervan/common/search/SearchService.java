package com.bervan.common.search;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchCriteria;
import com.bervan.common.search.model.SearchResponse;
import com.bervan.common.search.model.SortDirection;
import com.bervan.history.model.Persistable;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.internal.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SearchService {
    private final Logger log = LoggerFactory.getLogger(SearchService.class);
    @PersistenceContext
    protected EntityManager entityManager;
    protected CriteriaBuilder criteriaBuilder;

    private Object getPrimitiveTypeValue(Object value, Field field) {
        String valueAsString = String.valueOf(value);
        if (field.getAnnotatedType().getType().equals(Long.class)) {
            value = Long.valueOf(valueAsString);
        } else if (field.getAnnotatedType().getType().equals(String.class)) {
            value = valueAsString;
        } else if (field.getAnnotatedType().getType().equals(Integer.class)) {
            value = Integer.valueOf(valueAsString);
        } else if (field.getAnnotatedType().getType().equals(Double.class)) {
            value = Double.valueOf(valueAsString);
        }
        return value;
    }

    @PostConstruct
    public void init() {
        criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    public <T extends Persistable> SearchResponse<T> search(SearchRequest searchRequest, SearchQueryOption options) {
        try {
            init();
            validateOptions(options);
            Class<? extends BervanBaseEntity> entityToFind = getEntityToFind(options);

            SortDirection sortDirection = options.getSortDirection();
            String sortField = options.getSortField();
            Integer page = options.getPage();
            Integer pageSize = options.getPageSize();

            CriteriaQuery<? extends BervanBaseEntity> mainQuery = criteriaBuilder.createQuery(entityToFind);
            Root<? extends BervanBaseEntity> root = mainQuery.from(entityToFind);

            Predicate predicates = null;
            if (searchRequest != null && searchRequest.groups.size() > 0) {
                Collection<Predicate> allPredicates = buildMainPredicate(searchRequest, root, entityToFind).values();
                predicates = criteriaBuilder.and(allPredicates.toArray(allPredicates.toArray(new Predicate[0])));
                mainQuery.where(predicates);
            }

            mainQuery.orderBy(createOrder(criteriaBuilder, root, sortField, isAscendingSortDirection(sortDirection)));

            List<? extends BervanBaseEntity> resultList = new ArrayList<>();
            if (!options.isCountQuery()) {
                TypedQuery<? extends BervanBaseEntity> resultQuery = entityManager.createQuery(mainQuery);
                resultQuery.setFirstResult(pageSize * (page));
                resultQuery.setMaxResults(pageSize);
                resultList = resultQuery.getResultList();
            }
            Long allFound = getHowManyItemsExist(searchRequest, entityToFind);

            return new SearchResponse(resultList, resultList.size(), page, allFound);
        } catch (Exception e) {
            log.error("Could not perform search!", e);
            throw new RuntimeException("Could not perform search!");
        }
    }

    private Order createOrder(CriteriaBuilder cb, Root<?> root, String sortField, boolean ascending) {
        if (ascending) {
            return cb.asc(root.get(sortField));
        } else {
            return cb.desc(root.get(sortField));
        }
    }

    private void validateOptions(SearchQueryOption options) {
        throwIfNullOrEmpty(options.getPage(), "page");
        throwIfNullOrEmpty(options.getPageSize(), "pageSize");
        throwIfNullOrEmpty(options.getSortField(), "sortField");
        throwIfNullOrEmpty(options.getSortDirection(), "sortDirection");
    }

    private void throwIfNullOrEmpty(Object option, String name) {
        if (Objects.isNull(option) || StringHelper.isEmpty(String.valueOf(option))) {
            throw new RuntimeException(String.format("Option %s cannot be empty!", name));
        }
    }

    public Class<? extends BervanBaseEntity> getEntityToFind(SearchQueryOption options) {
        return options.getEntityToFind();
    }

    private Long getHowManyItemsExist(SearchRequest searchRequest, Class<? extends BervanBaseEntity> entityToFind) throws NoSuchFieldException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<? extends BervanBaseEntity> newR = countQuery.from(entityToFind);
        Collection<Predicate> allPredicates = buildMainPredicate(searchRequest, newR, entityToFind).values();
        Predicate predicate = criteriaBuilder.and(allPredicates.toArray(allPredicates.toArray(new Predicate[0])));
        if (predicate != null) {
            countQuery.select(cb.count(newR)).where(predicate);
        } else {
            countQuery.select(cb.count(newR));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private boolean isAscendingSortDirection(SortDirection sortDirection) {
        return sortDirection.equals(SortDirection.ASC);
    }

    private Map<String, Predicate> buildMainPredicate(SearchRequest searchRequest, Root<? extends BervanBaseEntity> root, Class<? extends BervanBaseEntity> entityToFind) throws NoSuchFieldException {
        Map<String, Predicate> groupPredicate = new HashMap<>();

        Group lastProcessed = null;
        for (Group group : searchRequest.groups) {
            lastProcessed = group;
            List<Predicate> predicatesForGroup = new ArrayList<>();
            for (String queryId : group.criteriaIds) {
                if (groupPredicate.containsKey(queryId)) {
                    predicatesForGroup.add(groupPredicate.get(queryId));
                } else {
                    Criterion queryCriterion = searchRequest.criteria.stream().filter(criterion -> criterion.id.equals(queryId)).findFirst().get();

                    if (queryCriterion.attr.startsWith("[")) {
                        //join
                        String joinFieldName = queryCriterion.attr.substring(1, queryCriterion.attr.indexOf("]"));
                        Join join = root.join(joinFieldName);
                        Class javaType = join.getJavaType();
                        String newAttr = queryCriterion.attr.replace("[" + joinFieldName + "].", "");
                        Criterion newQueryCriterion = new Criterion(UUID.randomUUID().toString(), javaType.getSimpleName(), newAttr, queryCriterion.operator, queryCriterion.value);
                        Predicate predicate = buildPredicateForNotCollection(join, javaType, newQueryCriterion);
                        predicatesForGroup.add(predicate);
                    } else {
                        Predicate predicate = buildPredicateForNotCollection(root, entityToFind, queryCriterion);
                        predicatesForGroup.add(predicate);
                    }
                }

                if (group.operator.equals(Operator.AND_OPERATOR)) {
                    groupPredicate.put(group.id, criteriaBuilder.and(predicatesForGroup.toArray(Predicate[]::new)));
                } else if (group.operator.equals(Operator.OR_OPERATOR)) {
                    groupPredicate.put(group.id, criteriaBuilder.or(predicatesForGroup.toArray(Predicate[]::new)));
                } else {
                    log.warn("Empty operator, default is AND!");
                    groupPredicate.put(group.id, criteriaBuilder.and(predicatesForGroup.toArray(Predicate[]::new)));
                }
            }
        }
        return groupPredicate;
    }

    private Predicate buildPredicateForNotCollection(From root, Class<? extends BervanBaseEntity> entityToFind, Criterion queryCriterion) throws NoSuchFieldException {
        String field = queryCriterion.type + "." + queryCriterion.attr;
        SearchCriteria entityCriterion = new SearchCriteria(field, null, getValue(queryCriterion.value, field, entityToFind));

        Predicate predicate = null;
        switch (queryCriterion.operator) {
            case EQUALS_OPERATION -> predicate = SearchOperationsHelper.equal(root, criteriaBuilder, entityCriterion);
            case LIKE_OPERATION -> predicate = SearchOperationsHelper.contains(root, criteriaBuilder, entityCriterion);
            case NOT_EQUALS_OPERATION ->
                    predicate = SearchOperationsHelper.notEqual(root, criteriaBuilder, entityCriterion);
            case NOT_LIKE_OPERATION ->
                    predicate = SearchOperationsHelper.notContains(root, criteriaBuilder, entityCriterion);
            case IN_OPERATION -> predicate = SearchOperationsHelper.in(root, entityCriterion);

            default -> log.error("NULL PREDICATE, INVALID OPERATOR!!!");
        }
        return predicate;
    }

    private Field getDeclaredField(String fieldName, Field field, Class<? extends BervanBaseEntity> entity) throws NoSuchFieldException {
        try {
            return field.getType().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                return entity.getSuperclass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException x) {
                return entity.getSuperclass().getSuperclass().getDeclaredField(fieldName);
            }
        }
    }

    private Object getValue(Object value, Field field) {
        if (value instanceof Collection<?>) {
            List<Object> result = new ArrayList<>();
            ((Collection<?>) value).forEach(e -> result.add(getValueForSingularType(e, field)));
            return result;
        }

        return getValueForSingularType(value, field);
    }

    private Object getValueForSingularType(Object value, Field field) {
        if (field.getType().isEnum()) {
            value = getEnumValue(value, field);
        } else {
            value = getPrimitiveTypeValue(value, field);
        }

        if (value == null) {
            log.warn("Query parameter \"" + field.getName() + "\" is empty!");
        }

        return value;
    }

    private Object getValue(Object value, String field, Class<? extends BervanBaseEntity> entityToFind) throws NoSuchFieldException {
        String[] subObjects = field.split("\\.");
        String fst = subObjects[0];
        int i = 1;
        if (fst.equalsIgnoreCase(entityToFind.getSimpleName())) {
            fst = subObjects[1];
            i = 2;
        }

        Field declaredField = entityToFind.getDeclaredField(fst);

        for (; i < subObjects.length; i++) {
            declaredField = getDeclaredField(subObjects[i], declaredField, entityToFind);
        }

        return getValue(value, declaredField);
    }

    private void setValueAsArray(SearchCriteria entityCriterion, Field field) {
        String[] val = String.valueOf(entityCriterion.getValue()).split(",");
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < val.length; i++) {
            values.add(getValue(val[i], field));
        }

        entityCriterion.setValue(values);
    }

    private Enum getEnumValue(Object value, Field field) {
        Optional<?> el = Arrays.stream(field.getType().getEnumConstants())
                .filter(e -> ((Enum) e).name().equals(value))
                .findFirst();
        return (Enum) el.orElse(null);
    }
}
