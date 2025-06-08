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

import static com.bervan.common.search.SearchRequest.FINAL_GROUP_CONSTANT;

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

            CriteriaQuery mainQuery;
            Root<? extends BervanBaseEntity> root;

            if (options.getColumnsToFetch() != null && !options.getColumnsToFetch().isEmpty()) {
                mainQuery = criteriaBuilder.createQuery(Object[].class);
                root = mainQuery.from(entityToFind);

                List<String> columnsToFetch = options.getColumnsToFetch();
                Selection<?>[] selections = columnsToFetch.stream()
                        .map(root::get)
                        .toArray(Selection[]::new);

                mainQuery.select(criteriaBuilder.array(selections));
            } else {
                mainQuery = criteriaBuilder.createQuery(entityToFind);
                root = mainQuery.from(entityToFind);
            }

            Predicate predicates = null;
            if (searchRequest != null && searchRequest.groups.size() > 0) {
                predicates = buildMainPredicate(searchRequest, root, entityToFind);
                mainQuery.where(predicates);
            }

            mainQuery.orderBy(createOrder(criteriaBuilder, root, sortField, isAscendingSortDirection(sortDirection)));

            List resultList = new ArrayList<>();
            if (!options.isCountQuery()) {
                TypedQuery resultQuery = entityManager.createQuery(mainQuery);
                resultQuery.setFirstResult(pageSize * (page));
                resultQuery.setMaxResults(pageSize);
                resultList = resultQuery.getResultList();
            }

            if (options.getColumnsToFetch() != null && !options.getColumnsToFetch().isEmpty()) {
                resultList = resultList.stream().map(row -> {
                    try {
                        T instance = (T) entityToFind.getDeclaredConstructor().newInstance();
                        for (int i = 0; i < options.getColumnsToFetch().size(); i++) {
                            Field field = entityToFind.getDeclaredField(options.getColumnsToFetch().get(i));
                            field.setAccessible(true);
                            field.set(instance, ((Object[]) row)[i]);
                        }
                        return instance;
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping row to entity", e);
                    }
                }).toList();
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

    private Class<? extends BervanBaseEntity> getEntityToFind(SearchQueryOption options) {
        return options.getEntityToFind();
    }

    private Long getHowManyItemsExist(SearchRequest searchRequest, Class<? extends BervanBaseEntity> entityToFind) throws NoSuchFieldException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<? extends BervanBaseEntity> newR = countQuery.from(entityToFind);
        Predicate predicate = buildMainPredicate(searchRequest, newR, entityToFind);
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

    private Predicate buildMainPredicate(SearchRequest searchRequest, Root<? extends BervanBaseEntity> root, Class<? extends BervanBaseEntity> entityToFind) throws NoSuchFieldException {
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


        boolean mergedGroupExists = false;

        for (Map.Entry<String, Map<Operator, List<String>>> mergedGroups : searchRequest.mergedGroups.entrySet()) {
            String groupId = mergedGroups.getKey();
            Map<Operator, List<String>> value = mergedGroups.getValue();
            List<Predicate> innerGroupsPredicates = new ArrayList<>();
            List<String> innerGroups;
            Operator groupsOperator;

            if (value.get(Operator.AND_OPERATOR) != null) {
                innerGroups = value.get(Operator.AND_OPERATOR);
                groupsOperator = Operator.AND_OPERATOR;
            } else {
                innerGroups = value.get(Operator.OR_OPERATOR);
                groupsOperator = Operator.OR_OPERATOR;
            }

            for (String innerGroup : innerGroups) {
                innerGroupsPredicates.add(groupPredicate.get(innerGroup));
            }

            if (groupsOperator == Operator.AND_OPERATOR) {
                groupPredicate.put(groupId, criteriaBuilder.and(innerGroupsPredicates.toArray(Predicate[]::new)));
            } else {
                groupPredicate.put(groupId, criteriaBuilder.or(innerGroupsPredicates.toArray(Predicate[]::new)));
            }
            mergedGroupExists = true;
        }

        if (groupPredicate.containsKey(FINAL_GROUP_CONSTANT)) {
            return groupPredicate.get(FINAL_GROUP_CONSTANT);
        }

        if (mergedGroupExists) {
            throw new RuntimeException("You've created group(s) with other groups predicate. Final group must be created with: " + FINAL_GROUP_CONSTANT + " group ID!!");
        }

        //create predicate with default operator for all groups
        return criteriaBuilder.and(groupPredicate.values().toArray(Predicate[]::new));
    }

    private Predicate buildPredicateForNotCollection(From root, Class<? extends BervanBaseEntity> entityToFind, Criterion queryCriterion) throws NoSuchFieldException {
        String field = queryCriterion.type + "." + queryCriterion.attr;
        SearchCriteria entityCriterion = new SearchCriteria(field, null, getValue(queryCriterion.value, field, entityToFind));

        Predicate predicate = null;
        switch (queryCriterion.operator) {
            case IS_NULL_OPERATION -> predicate = SearchOperationsHelper.isNull(root, criteriaBuilder, entityCriterion);
            case IS_NOT_NULL_OPERATION ->
                    predicate = SearchOperationsHelper.isNotNull(root, criteriaBuilder, entityCriterion);
            case EQUALS_OPERATION -> predicate = SearchOperationsHelper.equal(root, criteriaBuilder, entityCriterion);
            case GREATER_EQUAL_OPERATION ->
                    predicate = SearchOperationsHelper.greaterEqual(root, criteriaBuilder, entityCriterion);
            case LESS_EQUAL_OPERATION ->
                    predicate = SearchOperationsHelper.lessEqual(root, criteriaBuilder, entityCriterion);
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
