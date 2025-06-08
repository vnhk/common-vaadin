package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchRequestQueryTranslator {
    public static SearchRequest translateQuery(String query, Class<?> entityToFind) {
        SearchRequest searchRequest = new SearchRequest();
        AtomicInteger criterionCounter = new AtomicInteger(1);
        AtomicInteger groupCounter = new AtomicInteger(1);

        Expression parsedExpression = parseExpression(query);

        String rootGroupId = buildSearchRequestFromExpression(parsedExpression, entityToFind, searchRequest, criterionCounter, groupCounter);

        if (!SearchRequest.FINAL_GROUP_CONSTANT.equals(rootGroupId)) {
            searchRequest.renameMergeGroup(rootGroupId, SearchRequest.FINAL_GROUP_CONSTANT);
        }

        return searchRequest;
    }

    private static Expression parseExpression(String input) {
        input = input.trim();
        if (input.startsWith("(") && input.endsWith(")")) {
            return parseExpression(input.substring(1, input.length() - 1));
        }

        int parens = 0;
        List<String> tokens = new ArrayList<>();
        List<Operator> ops = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '(') parens++;
            if (c == ')') parens--;

            if ((c == '&' || c == '|') && parens == 0) {
                tokens.add(current.toString().trim());
                current.setLength(0);
                ops.add(c == '&' ? Operator.AND_OPERATOR : Operator.OR_OPERATOR);
                i++; // skip next char if it's part of &&
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString().trim());

        if (tokens.size() == 1) {
            return parseSimple(tokens.get(0));
        }

        // assume all ops are same (AND/OR)
        Operator op = ops.get(0);
        List<Expression> expressions = tokens.stream().map(SearchRequestQueryTranslator::parseExpression).toList();
        return new GroupExpression(op, expressions);
    }

    private static Expression parseSimple(String input) {
        SearchOperation op;
        String attr, value;

        if (input.contains("!=")) {
            op = SearchOperation.NOT_EQUALS_OPERATION;
            String[] parts = input.split("!=");
            attr = parts[0].trim();
            value = parts[1].trim().replaceAll("^'+|'+$", "");
        } else if (input.contains("=")) {
            op = SearchOperation.EQUALS_OPERATION;
            String[] parts = input.split("=");
            attr = parts[0].trim();
            value = parts[1].trim().replaceAll("^'+|'+$", "");
        } else {
            throw new IllegalArgumentException("Invalid condition: " + input);
        }

        return new Condition(attr, op, value);
    }

    private static String buildSearchRequestFromExpression(
            Expression expr,
            Class<?> entityClass,
            SearchRequest request,
            AtomicInteger criterionIdCounter,
            AtomicInteger groupIdCounter
    ) {
        if (expr instanceof Condition cond) {
            String cid = "C" + criterionIdCounter.getAndIncrement();
            Criterion criterion = new Criterion(cid, entityClass.getSimpleName(), cond.attribute, cond.operation, cond.value);
            String gid = "G" + groupIdCounter.getAndIncrement();
            request.addCriterion(gid, Operator.AND_OPERATOR, criterion);
            return gid;
        }

        GroupExpression groupExpr = (GroupExpression) expr;
        List<String> innerGroupIds = new ArrayList<>();

        for (Expression subExpr : groupExpr.expressions) {
            String gid = buildSearchRequestFromExpression(subExpr, entityClass, request, criterionIdCounter, groupIdCounter);
            innerGroupIds.add(gid);
        }

        String thisGroupId = "G" + groupIdCounter.getAndIncrement();
        request.mergeGroup(thisGroupId, groupExpr.operator, innerGroupIds.toArray(new String[0]));
        return thisGroupId;
    }

    public static interface Expression {
    }

    public static class Condition implements Expression {
        public final String attribute;
        public final SearchOperation operation;
        public final String value;

        public Condition(String attribute, SearchOperation operation, String value) {
            this.attribute = attribute;
            this.operation = operation;
            this.value = value;
        }
    }

    public static class GroupExpression implements Expression {
        public final Operator operator;
        public final List<Expression> expressions;

        public GroupExpression(Operator operator, List<Expression> expressions) {
            this.operator = operator;
            this.expressions = expressions;
        }
    }
}