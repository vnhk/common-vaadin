package com.bervan.common.search;

import com.bervan.common.search.model.SearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.stream.Collectors;

public class SearchOperationsHelper {

    public static Predicate notLike(From root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.notLike(getExpression(root, entityCriterion.getField()), String.valueOf(entityCriterion.getValue()));
    }

    public static Predicate notEqual(From root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.notEqual(getExpression(root, entityCriterion.getField()), entityCriterion.getValue());
    }

    public static Predicate equal(From root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.equal(getExpression(root, entityCriterion.getField()), entityCriterion.getValue());
    }

    public static Predicate like(From root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.like(getExpression(root, entityCriterion.getField()), String.valueOf(entityCriterion.getValue()));
    }

    public static Path<String> getExpression(From root, String field) {
        String[] subObjects = field.split("\\.");
        String fst = subObjects[0];
        int i = 1;
        String entityName = root.getModel().getBindableJavaType().getSimpleName();

        if (fst.equalsIgnoreCase(entityName)) {
            fst = subObjects[1];
            i = 2;
        }

        Path<String> objectPath = root.get(fst);

        for (; i < subObjects.length; i++) {
            objectPath = objectPath.get(subObjects[i]);
        }


        return objectPath;
    }

    public static Predicate in(From root, SearchCriteria entityCriterion) {
        Object value = entityCriterion.getValue();
        return getExpression(root, entityCriterion.getField()).in(((Collection) value));
    }

    public static Predicate contains(From root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        entityCriterion.setValue("%" + entityCriterion.getValue() + "%");
        return like(root, criteriaBuilder, entityCriterion);
    }

    public static Predicate notContains(From root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        entityCriterion.setValue("%" + entityCriterion.getValue() + "%");
        return notLike(root, criteriaBuilder, entityCriterion);
    }
}
