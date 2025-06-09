package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.AuthService;

import java.io.Serializable;
import java.util.*;

public class SearchRequest {
    public static final String OWNER_ACCESS_GROUP = "OWNER_ACCESS_GROUP";
    public static String FINAL_GROUP_CONSTANT = "FINAL_GROUP";
    public final List<Group> groups = new ArrayList<>();
    public final List<Criterion> criteria = new ArrayList<>();
    public final Map<String, Map<Operator, List<String>>> mergedGroups = new HashMap<>();
    private boolean addOwnerCriterion = true;

    public void addCriterion(String groupId, Operator groupOperatorForNewGroup, Class<?> objectType, String fieldPath, SearchOperation fieldValueOperator, Object value) {
        Group groupToUpdate;
        Optional<Group> group = groups.stream().filter(e -> e.id.equals(groupId)).findFirst();
        if (group.isEmpty()) {
            groupToUpdate = new Group(groupId, groupOperatorForNewGroup);
            groups.add(groupToUpdate);
        } else {
            groupToUpdate = group.get();
        }

        String criteriaId = UUID.randomUUID().toString();
        criteria.add(new Criterion(criteriaId, objectType.getSimpleName(), fieldPath, fieldValueOperator, value));
        groupToUpdate.criteriaIds.add(criteriaId);
    }

    public void addCriterion(String groupId, Class<?> objectType, String fieldPath, SearchOperation fieldValueOperator, Object value) {
        addCriterion(groupId, Operator.AND_OPERATOR, objectType, fieldPath, fieldValueOperator, value);
    }

    public void addCriterion(String groupId, Operator groupOperatorForNewGroup, Criterion... criteria) {
        Group groupToUpdate;
        Optional<Group> group = groups.stream().filter(e -> e.id.equals(groupId)).findFirst();
        if (group.isEmpty()) {
            groupToUpdate = new Group(groupId, groupOperatorForNewGroup);
            groups.add(groupToUpdate);
        } else {
            groupToUpdate = group.get();
        }

        for (Criterion criterion : criteria) {
            if (criterion.id == null) {
                String criteriaId = UUID.randomUUID().toString();
                groupToUpdate.criteriaIds.add(criteriaId);
            } else {
                groupToUpdate.criteriaIds.add(criterion.id);
            }
            this.criteria.add(criterion);
        }
    }

    public boolean isAddOwnerCriterion() {
        return addOwnerCriterion;
    }

    public void setAddOwnerCriterion(boolean addOwnerCriterion) {
        this.addOwnerCriterion = addOwnerCriterion;
    }

    public void addDeletedFalseCriteria(Class<?> objectType) {
        addCriterion("DELETED_FALSE_CRITERIA_GROUP", Operator.OR_OPERATOR, objectType, "deleted", SearchOperation.EQUALS_OPERATION, false);
        addCriterion("DELETED_FALSE_CRITERIA_GROUP", Operator.OR_OPERATOR, objectType, "deleted", SearchOperation.IS_NULL_OPERATION, null);
    }

    public void addOwnerAccessCriteria(Class<?> objectType) {
        if (addOwnerCriterion) {
            addCriterion(OWNER_ACCESS_GROUP, objectType, "[owners].id", SearchOperation.EQUALS_OPERATION, AuthService.getLoggedUserId());
        }
    }

    public void addOwnerAccessCriteria(Class<?> objectType, UUID userId) {
        if (addOwnerCriterion) {
            addCriterion(OWNER_ACCESS_GROUP, objectType, "[owners].id", SearchOperation.EQUALS_OPERATION, userId);
        }
    }

    public boolean containsGroup(String groupId) {
        return criteria.stream().anyMatch(e -> e.id.equals(groupId));
    }

    public void addIdEqualsCriteria(String groupId, Class<?> objectType, Serializable id) {
        addCriterion(groupId, objectType, "id", SearchOperation.EQUALS_OPERATION, id);
    }

    public void merge(SearchRequest request) {
        groups.addAll(request.groups);
        criteria.addAll(request.criteria);
    }

    public void mergeGroup(String newGroup, Operator operator, String... groups) {
        Map<Operator, List<String>> innerMap = new HashMap<>();
        innerMap.put(operator, List.of(groups));
        mergedGroups.put(newGroup, innerMap);
    }

    public void renameMergeGroup(String oldId, String newId) {
        if (groups.size() == 1) {
            return;
        }
        if (!mergedGroups.containsKey(oldId)) {
            throw new IllegalArgumentException("Group with id " + oldId + " does not exist");
        }
        if (mergedGroups.containsKey(newId)) {
            throw new IllegalArgumentException("Group with id " + newId + " already exists");
        }
        mergedGroups.put(newId, mergedGroups.remove(oldId));
    }
}