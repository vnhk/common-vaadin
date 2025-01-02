package com.bervan.common.search;

import com.bervan.common.search.model.*;
import com.bervan.history.model.AbstractBaseEntity;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
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

    public SearchResponse search(String query, SearchQueryOption options) {
        init();
        validateOptions(options);
        Class<? extends AbstractBaseEntity> entityToFind = getEntityToFind(options);

        SortDirection sortDirection = options.getSortDirection();
        String sortField = options.getSortField();
        Integer page = options.getPage();
        Integer pageSize = options.getPageSize();

        CriteriaQuery<? extends AbstractBaseEntity> criteriaQuery = criteriaBuilder.createQuery(entityToFind);
        Root<? extends AbstractBaseEntity> root = criteriaQuery.from(entityToFind);

        if (StringUtils.isNoneEmpty(query)) {
            criteriaQuery.where(buildMainPredicate(query, root, entityToFind));
        }

        criteriaQuery.orderBy(new OrderImpl(SearchOperationsHelper.getExpression(root, sortField), isAscendingSortDirection(sortDirection)));

        TypedQuery<? extends AbstractBaseEntity> resultQuery = entityManager.createQuery(criteriaQuery);
        Integer allFound = getHowManyItemsExist(criteriaQuery, root);

        resultQuery.setFirstResult(pageSize * (page - 1));
        resultQuery.setMaxResults(pageSize);
        List<? extends AbstractBaseEntity> resultList = resultQuery.getResultList();

        return new SearchResponse(resultList, resultList.size(), page, allFound);
    }

    public SearchResponse search(SearchRequest searchRequest, SearchQueryOption options) {
        try {
            init();
            validateOptions(options);
            Class<? extends AbstractBaseEntity> entityToFind = getEntityToFind(options);

            SortDirection sortDirection = options.getSortDirection();
            String sortField = options.getSortField();
            Integer page = options.getPage();
            Integer pageSize = options.getPageSize();

            CriteriaQuery<? extends AbstractBaseEntity> criteriaQuery = criteriaBuilder.createQuery(entityToFind);
            Root<? extends AbstractBaseEntity> root = criteriaQuery.from(entityToFind);

            if (searchRequest != null && searchRequest.groups != null && searchRequest.groups.size() > 0) {
                criteriaQuery.where(buildMainPredicate(searchRequest, root, entityToFind));
            }

            criteriaQuery.orderBy(new OrderImpl(SearchOperationsHelper.getExpression(root, sortField), isAscendingSortDirection(sortDirection)));

            TypedQuery<? extends AbstractBaseEntity> resultQuery = entityManager.createQuery(criteriaQuery);
            Integer allFound = getHowManyItemsExist(criteriaQuery, root);

            resultQuery.setFirstResult(pageSize * (page - 1));
            resultQuery.setMaxResults(pageSize);
            List<? extends AbstractBaseEntity> resultList = resultQuery.getResultList();

            return new SearchResponse(resultList, resultList.size(), page, allFound);
        } catch (Exception e) {
            log.error("Could not perform search!", e);
            throw new RuntimeException("Could not perform search!");
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

    public Class<? extends AbstractBaseEntity> getEntityToFind(SearchQueryOption options) {
//        if (options.getEntityToFind().equals("project")) {
//            return Project.class;
//        } else if (options.getEntityToFind().equals("task")) {
//            return Task.class;
//        } else if (options.getEntityToFind().equals("userAccount")) {
//            return UserAccount.class;
//        }

        try {
            return (Class<? extends AbstractBaseEntity>) Class.forName(options.getEntityToFind());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not create query for " + options.getEntityToFind() + "!");
        }
    }

    private Integer getHowManyItemsExist(CriteriaQuery<? extends AbstractBaseEntity> criteriaQuery, Root<? extends AbstractBaseEntity> root) {
        criteriaQuery.select(root.get("id"));
        return entityManager.createQuery(criteriaQuery).getResultList().size();
    }

    private boolean isAscendingSortDirection(SortDirection sortDirection) {
        return sortDirection.equals(SortDirection.ASC);
    }

    private Predicate buildMainPredicate(String query, Root<? extends AbstractBaseEntity> root, Class<? extends AbstractBaseEntity> entityToFind) {
        log.info(String.format("Processing query:[%s]", query));

        List<Operator> operators = new ArrayList<>();
        query = replaceAndAddOperators(query, operators);

        if (operators.isEmpty()) {
            log.info("Query contains one sub query.");
            return buildPredicate(query, root, entityToFind);
        } else {
            log.info("Query contains multiple sub queries.");
            return buildPredicate(query, root, entityToFind, operators);
        }
    }

    private Predicate buildMainPredicate(SearchRequest searchRequest, Root<? extends AbstractBaseEntity> root, Class<? extends AbstractBaseEntity> entityToFind) throws NoSuchFieldException {
        Map<String, Predicate> groupPredicate = new HashMap<>();

        int actualGroup = 1;
        Optional<Group> groupOpt = searchRequest.groups.stream().filter(g -> g.id.equals("G1"))
                .findFirst();
        do {
            Group group = groupOpt.get();
            List<Predicate> predicatesForGroup = new ArrayList<>();
            for (String queryId : group.queries) {
                if (groupPredicate.containsKey(queryId)) {
                    predicatesForGroup.add(groupPredicate.get(queryId));
                } else {
                    Criterion queryCriterion = searchRequest.criteria.stream().filter(criterion -> criterion.id.equals(queryId))
                            .findFirst().get();

//                    if (queryCriterion.type.startsWith("[") && queryCriterion.type.endsWith("]")) {
//                        Criterion newCriterion = new Criterion();
//                        newCriterion.value = queryCriterion.value;
//                        newCriterion.id = queryCriterion.id;
//                        newCriterion.operator = queryCriterion.operator;
//                        newCriterion.attr = queryCriterion.attr;
//                        List<Project> projects = null;
//                        if (queryCriterion.type.equals("[addedToProjects]")) {
//                            CriteriaQuery<UserProject> userProjectQuery = criteriaBuilder.createQuery(UserProject.class);
//                            Root<UserProject> rootUserProject = userProjectQuery.from(UserProject.class);
//                            newCriterion.type = "user";
//
//                            Predicate predicate = buildPredicateForNotCollection(rootUserProject, UserProject.class, newCriterion);
//                            userProjectQuery.where(predicate);
//                            List<UserProject> resultList = entityManager.createQuery(userProjectQuery).getResultList();
//                            projects = resultList.stream().map(UserProject::getProject).collect(Collectors.toList());
//                        } else if (queryCriterion.type.equals("[tasks]")) {
//                            CriteriaQuery<Task> projectTaskQuery = criteriaBuilder.createQuery(Task.class);
//                            Root<Task> rootTask = projectTaskQuery.from(Task.class);
//                            newCriterion.type = "task";
//
//                            Predicate predicate = buildPredicateForNotCollection(rootTask, Task.class, newCriterion);
//                            projectTaskQuery.where(predicate);
//                            List<Task> resultList = entityManager.createQuery(projectTaskQuery).getResultList();
//                            projects = resultList.stream().map(Task::getProject).collect(Collectors.toList());
//                        }
//
//                        Predicate addedToProjects = root.in(projects);
//                        predicatesForGroup.add(addedToProjects);
//                    } else {
//                        Predicate predicate = buildPredicateForNotCollection(root, entityToFind, queryCriterion);
//                        predicatesForGroup.add(predicate);
//                    }
                }

                if (group.operator.equals("AND")) {
                    groupPredicate.put(group.id, criteriaBuilder.and(predicatesForGroup.toArray(Predicate[]::new)));
                } else if (group.operator.equals("OR")) {
                    groupPredicate.put(group.id, criteriaBuilder.or(predicatesForGroup.toArray(Predicate[]::new)));
                } else {
                    log.warn("Empty operator, default is AND!");
                    groupPredicate.put(group.id, criteriaBuilder.and(predicatesForGroup.toArray(Predicate[]::new)));
                }
            }
            actualGroup++;
            int finalActualGroup = actualGroup;
            groupOpt = searchRequest.groups.stream().filter(g -> g.id.equals("G" + finalActualGroup))
                    .findFirst();
        } while (groupOpt.isPresent());

        return groupPredicate.get("G" + (actualGroup - 1));
    }

    private Predicate buildPredicateForNotCollection(Root<? extends AbstractBaseEntity> root, Class<? extends AbstractBaseEntity> entityToFind, Criterion queryCriterion) throws NoSuchFieldException {
        String field = queryCriterion.type + "." + queryCriterion.attr;
        SearchCriteria entityCriterion = new SearchCriteria(field, null, getValue(queryCriterion.value, field, entityToFind));

        Predicate predicate = null;
        switch (queryCriterion.operator) {
            case "equals" -> predicate = SearchOperationsHelper.equal(root, criteriaBuilder, entityCriterion);
            case "contains" -> predicate = SearchOperationsHelper.contains(root, criteriaBuilder, entityCriterion);
            case "notEquals" -> predicate = SearchOperationsHelper.notEqual(root, criteriaBuilder, entityCriterion);
            case "notContains" ->
                    predicate = SearchOperationsHelper.notContains(root, criteriaBuilder, entityCriterion);
            default -> log.error("NULL PREDICATE, INVALID OPERATOR!!!");
        }
        return predicate;
    }

    private Predicate buildPredicate(String queryWithOneSubQuery, Root<? extends AbstractBaseEntity> root, Class<? extends AbstractBaseEntity> entityToFind) {
        queryWithOneSubQuery = removeParentheses(queryWithOneSubQuery);
        SearchCriteria searchCriteria = buildSearchCriteria(queryWithOneSubQuery);
        log.info(String.format("Criteria created:[%s]", searchCriteria));

        return preparePredicates(root, searchCriteria, entityToFind);
    }

    private Predicate buildPredicate(String query, Root<? extends AbstractBaseEntity> root,
                                     Class<? extends AbstractBaseEntity> entityToFind, List<Operator> operators) {
        SearchCriteriaHolder mainCriteriaHolder = buildCriteriaHolder(query, operators);
        mainCriteriaHolder.setEntityToFind(entityToFind);
        QueryHolder holder = mainCriteriaHolder.getHolder();

        log.info("Building predicates.");
        Predicate predicate = buildPredicates(holder, mainCriteriaHolder, root);
        log.info(String.format("Predicates created:[%s]", predicate.toString()));

        return predicate;
    }

    private Predicate buildPredicates(QueryHolder holder, SearchCriteriaHolder mainCriteriaHolder, Root<? extends AbstractBaseEntity> root) {
        Class<? extends AbstractBaseEntity> entityToFind = mainCriteriaHolder.getEntityToFind();
        SearchCriteria fstSearchCriteria;
        SearchCriteria sndSearchCriteria;

        if (holder.getFstQuery() instanceof String fstQuery && holder.getSndQuery() instanceof String sndQuery) {
            fstSearchCriteria = getSearchCriteria(mainCriteriaHolder, fstQuery);
            sndSearchCriteria = getSearchCriteria(mainCriteriaHolder, sndQuery);

            List<Predicate> predicates = new ArrayList<>(2);
            predicates.add(preparePredicates(root, fstSearchCriteria, entityToFind));
            predicates.add(preparePredicates(root, sndSearchCriteria, entityToFind));

            return executeOperator(holder.getOperator(), predicates);
        } else if (holder.getFstQuery() instanceof String fstQuery) {
            fstSearchCriteria = getSearchCriteria(mainCriteriaHolder, fstQuery);

            List<Predicate> predicates = new ArrayList<>(2);
            predicates.add(preparePredicates(root, fstSearchCriteria, entityToFind));
            predicates.add(buildPredicates((QueryHolder) holder.getSndQuery(), mainCriteriaHolder, root));

            return executeOperator(holder.getOperator(), predicates);
        } else {
            sndSearchCriteria = getSearchCriteria(mainCriteriaHolder, (String) holder.getSndQuery());

            List<Predicate> predicates = new ArrayList<>(2);
            predicates.add(buildPredicates((QueryHolder) holder.getFstQuery(), mainCriteriaHolder, root));
            predicates.add(preparePredicates(root, sndSearchCriteria, entityToFind));

            return executeOperator(holder.getOperator(), predicates);
        }
    }

    private SearchCriteria getSearchCriteria(SearchCriteriaHolder mainCriteriaHolder, String query) {
        return mainCriteriaHolder.getSearchCriteria().entrySet().stream()
                .filter(e -> ("(" + e.getKey().getCode() + ")").equals(query))
                .map(Map.Entry::getValue).findFirst().get();
    }

    private SearchCriteriaHolder buildCriteriaHolder(String queryOrig, List<Operator> operators) {
        log.info("Building criteria holder.");

        SearchCriteriaHolder result = new SearchCriteriaHolder();
        Map<QueryMapping, SearchCriteria> searchCriteria = new HashMap<>();

        String query = queryOrig;

        String[] queries = query.split("LOGIC_OPR");
        log.info(String.format("queries:[%s]", Arrays.toString(queries)));

        int i = 0;
        for (String q : queries) {
            String replacement = "query" + i++;
            q = removeParentheses(q);
            searchCriteria.put(new QueryMapping(q, replacement), buildSearchCriteria(q));
            query = query.replace(q, replacement);

            log.info(String.format("query[%d]:[%s]", i, q));
        }

        QueryHolder holder = buildEntityCriteriaHolder(query, operators, searchCriteria);

        result.setSearchCriteria(searchCriteria);
        result.setHolder(holder);

        return result;
    }

    private String removeParentheses(String q) {
        return q.replace("(", "").replace(")", "").trim();
    }

    private QueryHolder buildEntityCriteriaHolder(String query, List<Operator> operators, Map<QueryMapping, SearchCriteria> searchCriteria) {
        List<QueryMapping> queries = new ArrayList<>();

        log.info(query);

        while (query.contains("LOGIC_OPR")) {
            int size = searchCriteria.size() + queries.size();
            for (int i = 0; i < size; i++) {
                for (int y = 0; y < size; y++) {
                    String connectedSubQuery = "(" + "query" + i + ")" + " LOGIC_OPR " + "(" + "query" + (y + 1) + ")";
                    if (query.contains(connectedSubQuery)) {
                        queries.add(new QueryMapping(connectedSubQuery, "query" + (size)));
                        query = query.replace(connectedSubQuery, "query" + (size));
                    }
                }
            }
        }

        assert operators.size() == queries.size();

        return createQueryHolders(operators, queries, queries.size() - 1);
    }

    private QueryHolder createQueryHolders(List<Operator> operators, List<QueryMapping> queries, int i) {
        QueryHolder holder = new QueryHolder();
        Operator operator = operators.get(i);
        QueryMapping queryMapping = queries.get(i);
        String code = queryMapping.getCode();
        String value = queryMapping.getQuery();
        holder.setCode(code);
        holder.setValue(value);
        holder.setOperator(operator);

        String[] args = holder.getValue().split(" LOGIC_OPR ");

        Optional<QueryMapping> first = queries.stream().filter(e -> ("(" + e.getCode() + ")").equals(args[0]))
                .findFirst();

        Optional<QueryMapping> second = queries.stream().filter(e -> ("(" + e.getCode() + ")").equals(args[1]))
                .findFirst();

        if (first.isEmpty()) {
            holder.setFstQuery(args[0]);
        } else {
            holder.setFstQuery(createQueryHolders(operators, queries, i - 1));
        }

        if (second.isEmpty()) {
            holder.setSndQuery(args[1]);
        } else {
            holder.setSndQuery(createQueryHolders(operators, queries, i - 1));
        }


        return holder;
    }

    private SearchCriteria buildSearchCriteria(String query) {
        SearchCriteria criteria = new SearchCriteria();
        for (SearchOperation searchOperation : SearchOperation.values()) {
            String[] criteriaArguments = query.split(searchOperation.name());
            if (criteriaArguments.length == 2) {
                criteria.setField(criteriaArguments[0].trim());
                criteria.setOperation(searchOperation);
                criteria.setValue(criteriaArguments[1].trim());
                break;
            }
        }
        return criteria;
    }

    private String replaceAndAddOperators(String query, List<Operator> operators) {
        int andIndex;
        int orIndex;
        do {
            andIndex = query.indexOf(Operator.AND_OPERATOR.name());
            orIndex = query.indexOf(Operator.OR_OPERATOR.name());

            if (orIndex == -1 && andIndex == -1) {
                break;
            }

            if (andIndex != -1 && orIndex != -1) {
                if (andIndex < orIndex) {
                    operators.add(Operator.AND_OPERATOR);
                    query = query.replaceFirst(Operator.AND_OPERATOR.name(), "LOGIC_OPR");
                } else {
                    operators.add(Operator.OR_OPERATOR);
                    query = query.replaceFirst(Operator.OR_OPERATOR.name(), "LOGIC_OPR");
                }
            } else if (andIndex != -1) {
                operators.add(Operator.AND_OPERATOR);
                query = query.replaceFirst(Operator.AND_OPERATOR.name(), "LOGIC_OPR");
            } else {
                operators.add(Operator.OR_OPERATOR);
                query = query.replaceFirst(Operator.OR_OPERATOR.name(), "LOGIC_OPR");
            }
        } while (true);

        return query;
    }

    private Predicate preparePredicates(Root<? extends AbstractBaseEntity> root, SearchCriteria searchCriteria, Class<? extends AbstractBaseEntity> entityToFind) {
        try {
            return execute(root, searchCriteria, entityToFind);
        } catch (NoSuchFieldException e) {
            log.error("Could not parse query, used field is not supported!", e);
            throw new RuntimeException("Could not parse query, used field is not supported!");
        }
    }

    private Predicate executeOperator(Operator operator, List<Predicate> predicates) {
        if (operator.equals(Operator.AND_OPERATOR)) {
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        } else if (operator.equals(Operator.OR_OPERATOR)) {
            return criteriaBuilder.or(predicates.toArray(Predicate[]::new));
        }

        throw new IllegalArgumentException("Invalid operator!");
    }

    private Predicate execute(Root<? extends AbstractBaseEntity> root, SearchCriteria entityCriterion, Class<? extends AbstractBaseEntity> entity) throws NoSuchFieldException {
        Object value;
        String[] subObjects = entityCriterion.getField().split("\\.");
        Field field = entity.getDeclaredField(subObjects[0]);

        for (int i = 1; i < subObjects.length; i++) {
            field = getDeclaredField(subObjects[i], field, entity);
        }

        if (entityCriterion.getOperation().equals(SearchOperation.IN_OPERATION)) {
            setValueAsArray(entityCriterion, field);
            return SearchOperationsHelper.in(root, entityCriterion);
        } else {
            value = getValue(entityCriterion.getValue(), field);
        }

        entityCriterion.setValue(value);

        Predicate result;
        switch (entityCriterion.getOperation()) {
            case EQUALS_OPERATION -> result = SearchOperationsHelper.equal(root, criteriaBuilder, entityCriterion);
            case LIKE_OPERATION -> result = SearchOperationsHelper.like(root, criteriaBuilder, entityCriterion);
            case NOT_EQUALS_OPERATION ->
                    result = SearchOperationsHelper.notEqual(root, criteriaBuilder, entityCriterion);
            case NOT_LIKE_OPERATION -> result = SearchOperationsHelper.notLike(root, criteriaBuilder, entityCriterion);
            default -> throw new IllegalArgumentException("Invalid SearchCriteria operation!");
        }

        return result;
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
