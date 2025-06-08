package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;

import java.util.ArrayList;
import java.util.List;

public class SearchRequestQueryTranslator {
    public static SearchRequest translateQuery(String query, Class<?> entityToFind) {
        SearchRequest searchRequest = new SearchRequest();

        String delimiter;
        Operator operator;
        if (query.contains("&")) {
            delimiter = "&";
            operator = Operator.AND_OPERATOR;
        } else if (query.contains("|")) {
            delimiter = "\\|";
            operator = Operator.OR_OPERATOR;
        } else {
            delimiter = null;
            operator = Operator.AND_OPERATOR;
        }

        String[] conditions = delimiter != null ? query.split(delimiter) : new String[]{query};

        List<Criterion> criterionList = new ArrayList<>();
        for (int i = 0; i < conditions.length; i++) {
            String condition = conditions[i].trim();
            String[] parts = condition.split("=");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid condition: " + condition);
            }

            String attr = parts[0].trim();
            String value = parts[1].trim().replaceAll("^'+|'+$", "");

            String criterionId = "C" + (i + 1);

            Criterion criterion = new Criterion(
                    criterionId,
                    entityToFind.getTypeName(),
                    attr,
                    SearchOperation.EQUALS_OPERATION,
                    value
            );

            criterionList.add(criterion);
        }

        String groupId = "G1";
        searchRequest.addCriterion(groupId, operator, criterionList.toArray(new Criterion[0]));

        return searchRequest;
    }
}