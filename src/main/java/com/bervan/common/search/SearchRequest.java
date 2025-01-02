package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SearchRequest {
    public final List<Group> groups = new ArrayList<>();
    public final List<Criterion> criteria = new ArrayList<>();

    public void addCriterion(String groupId, Class<?> objectType, String fieldPath, SearchOperation fieldValueOperator, Object value) {
        Group groupToUpdate;
        Optional<Group> group = groups.stream().filter(e -> e.id.equals(groupId)).findFirst();
        if (group.isEmpty()) {
            groupToUpdate = new Group(groupId, Operator.AND_OPERATOR);
            groups.add(groupToUpdate);
        } else {
            groupToUpdate = group.get();
        }

        String criteriaId = UUID.randomUUID().toString();
        criteria.add(new Criterion(criteriaId, objectType.getSimpleName(), fieldPath, fieldValueOperator, value));
        groupToUpdate.criteriaIds.add(criteriaId);
    }

    public void addDeletedFalseCriteria(String groupId, Class<?> objectType) {
        addCriterion(groupId, objectType, "deleted", SearchOperation.EQUALS_OPERATION, false);
    }

    public void addIdEqualsCriteria(String groupId, Class<?> objectType, UUID id) {
        addCriterion(groupId, objectType, "id", SearchOperation.EQUALS_OPERATION, id);
    }

    public void addOwnerAccessCriteria(String groupId, Class<?> objectType) {
        addCriterion(groupId, objectType, "[owners].id", SearchOperation.EQUALS_OPERATION, AuthService.getLoggedUserId());
    }
}