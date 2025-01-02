package com.bervan.common.search;

import com.bervan.common.search.model.SearchCriteria;
import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;

public class SearchOperationsHelper {

    public static Predicate notLike(Root<? extends AbstractBaseEntity> root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.notLike(getExpression(root, entityCriterion.getField()), String.valueOf(entityCriterion.getValue()));
    }

    public static Predicate notEqual(Root<? extends AbstractBaseEntity> root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.notEqual(getExpression(root, entityCriterion.getField()), entityCriterion.getValue());
    }

    public static Predicate equal(Root<? extends AbstractBaseEntity> root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.equal(getExpression(root, entityCriterion.getField()), entityCriterion.getValue());
    }

    public static Predicate like(Root<? extends AbstractBaseEntity> root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        return criteriaBuilder.like(getExpression(root, entityCriterion.getField()), String.valueOf(entityCriterion.getValue()));
    }

    public static Path<String> getExpression(Root<? extends AbstractBaseEntity> root, String field) {
        String[] subObjects = field.split("\\.");
        String fst = subObjects[0];
        int i = 1;
        String entityName = root.getModel().getName();

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

    public static Predicate in(Root<? extends AbstractBaseEntity> root, SearchCriteria entityCriterion) {
        return getExpression(root, entityCriterion.getField()).in(Arrays.asList(entityCriterion.getValue()));
    }

    public static Predicate contains(Root<? extends AbstractBaseEntity> root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        entityCriterion.setValue("%" + entityCriterion.getValue() + "%");
        return like(root, criteriaBuilder, entityCriterion);
    }

    public static Predicate notContains(Root<? extends AbstractBaseEntity> root, CriteriaBuilder criteriaBuilder, SearchCriteria entityCriterion) {
        entityCriterion.setValue("%" + entityCriterion.getValue() + "%");
        return notLike(root, criteriaBuilder, entityCriterion);
    }
}
