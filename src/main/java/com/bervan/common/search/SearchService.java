package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchCriteria;
import com.bervan.common.search.model.SearchResponse;
import com.bervan.common.search.model.SortDirection;
import com.bervan.history.model.AbstractBaseEntity;
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
        if (value == null) return null;

        String valueAsString = String.valueOf(value);
        if (field.getAnnotatedType().getType().equals(Long.class) || field.getAnnotatedType().getType().equals(long.class)) {
            value = Long.valueOf(valueAsString);
        } else if (field.getAnnotatedType().getType().equals(String.class)) {
            value = valueAsString;
        } else if (field.getAnnotatedType().getType().equals(Integer.class) || field.getAnnotatedType().getType().equals(int.class)) {
            value = Integer.valueOf(valueAsString);
        } else if (field.getAnnotatedType().getType().equals(Double.class) || field.getAnnotatedType().getType().equals(double.class)) {
            value = Double.valueOf(valueAsString);
        } else if (field.getAnnotatedType().getType().equals(Boolean.class) || field.getAnnotatedType().getType().equals(boolean.class)) {
            value = Boolean.valueOf(valueAsString);
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
            Class<? extends AbstractBaseEntity> entityToFind = getEntityToFind(options);

            SortDirection sortDirection = options.getSortDirection();
            String sortField = options.getSortField();
            Integer page = options.getPage();
            Integer pageSize = options.getPageSize();

            CriteriaQuery mainQuery;
            Root<? extends AbstractBaseEntity> root;

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
        throwIfNullOrEmpty(options.getEntityToFind(), "entityToFind");
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

    private Class<? extends AbstractBaseEntity> getEntityToFind(SearchQueryOption options) {
        return options.getEntityToFind();
    }

    private Long getHowManyItemsExist(SearchRequest searchRequest, Class<? extends AbstractBaseEntity> entityToFind) throws NoSuchFieldException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<? extends AbstractBaseEntity> newR = countQuery.from(entityToFind);
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

    private Predicate buildMainPredicate(SearchRequest searchRequest, Root<? extends AbstractBaseEntity> root, Class<? extends AbstractBaseEntity> entityToFind) throws NoSuchFieldException {
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

        List<String> mergedGroupsSorted = searchRequest.mergedGroups.keySet().stream().toList()
                .stream().sorted((a, b) -> {
                    if (a.equals(FINAL_GROUP_CONSTANT)) return 1;
                    if (b.equals(FINAL_GROUP_CONSTANT)) return -1;

                    int numA = Integer.parseInt(a.substring(1));
                    int numB = Integer.parseInt(b.substring(1));
                    return Integer.compare(numA, numB);
                })
                .toList();

        for (String groupId : mergedGroupsSorted) {
            Map<Operator, List<String>> value = searchRequest.mergedGroups.get(groupId);
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

        // Collect all groups that are part of any mergedGroup (as inner groups)
        Set<String> groupsInMergedGroups = new HashSet<>();
        for (Map<Operator, List<String>> mergedGroupValue : searchRequest.mergedGroups.values()) {
            for (List<String> innerGroups : mergedGroupValue.values()) {
                groupsInMergedGroups.addAll(innerGroups);
            }
        }

        // Find standalone groups (not part of any merged group, and not a merged group itself)
        List<Predicate> standalonePredicates = new ArrayList<>();
        for (Map.Entry<String, Predicate> entry : groupPredicate.entrySet()) {
            String groupId = entry.getKey();
            // Skip if this group is inside a merged group OR is the FINAL_GROUP itself
            if (!groupsInMergedGroups.contains(groupId) &&
                !searchRequest.mergedGroups.containsKey(groupId)) {
                standalonePredicates.add(entry.getValue());
            }
        }

        if (groupPredicate.containsKey(FINAL_GROUP_CONSTANT)) {
            Predicate finalGroupPredicate = groupPredicate.get(FINAL_GROUP_CONSTANT);

            // If there are standalone groups (like OWNER_ACCESS_GROUP, DELETED_FALSE_CRITERIA_GROUP),
            // combine them with FINAL_GROUP using AND
            if (!standalonePredicates.isEmpty()) {
                standalonePredicates.add(finalGroupPredicate);
                return criteriaBuilder.and(standalonePredicates.toArray(Predicate[]::new));
            }

            return finalGroupPredicate;
        }

        if (mergedGroupExists) {
            throw new RuntimeException("You've created group(s) with other groups predicate. Final group must be created with: " + FINAL_GROUP_CONSTANT + " group ID!!");
        }

        //create predicate with default operator for all groups
        return criteriaBuilder.and(groupPredicate.values().toArray(Predicate[]::new));
    }

    private Predicate buildPredicateForNotCollection(From root, Class<? extends AbstractBaseEntity> entityToFind, Criterion queryCriterion) throws NoSuchFieldException {
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
            case GREATER_OPERATION ->
                    predicate = SearchOperationsHelper.greaterThan(root, criteriaBuilder, entityCriterion);
            case LESS_OPERATION ->
                    predicate = SearchOperationsHelper.lessThan(root, criteriaBuilder, entityCriterion);
            case LIKE_OPERATION -> predicate = SearchOperationsHelper.contains(root, criteriaBuilder, entityCriterion);
            case NOT_EQUALS_OPERATION ->
                    predicate = SearchOperationsHelper.notEqual(root, criteriaBuilder, entityCriterion);
            case NOT_LIKE_OPERATION ->
                    predicate = SearchOperationsHelper.notContains(root, criteriaBuilder, entityCriterion);
            case IN_OPERATION -> predicate = SearchOperationsHelper.in(root, entityCriterion);
            case NOT_IN_OPERATION -> predicate = SearchOperationsHelper.notIn(root, entityCriterion);

            default -> log.error("NULL PREDICATE, INVALID OPERATOR!!!");
        }
        return predicate;
    }

    private Field getDeclaredField(String fieldName, Field field, Class<? extends AbstractBaseEntity> entity) throws NoSuchFieldException {
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

    private Object getValue(Object value, String field, Class<? extends AbstractBaseEntity> entityToFind) throws NoSuchFieldException {
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
