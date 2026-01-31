package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.logging.LogEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SearchRequestQueryTranslatorTest {

    @Test
    void translateQuery_1() {
        SearchRequest searchRequest = SearchRequestQueryTranslator.translateQuery("""
                logLevel = 'ERROR' & methodName = 'translate'
                """, LogEntity.class);

        Assertions.assertNotNull(searchRequest);
        Assertions.assertEquals(2, searchRequest.criteria.size());
        Assertions.assertEquals("C1", searchRequest.criteria.get(0).id);
        Assertions.assertEquals(LogEntity.class.getSimpleName(), searchRequest.criteria.get(0).type);
        Assertions.assertEquals("logLevel", searchRequest.criteria.get(0).attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, searchRequest.criteria.get(0).operator);
        Assertions.assertEquals("ERROR", searchRequest.criteria.get(0).value);

        Assertions.assertEquals("C2", searchRequest.criteria.get(1).id);
        Assertions.assertEquals(LogEntity.class.getSimpleName(), searchRequest.criteria.get(1).type);
        Assertions.assertEquals("methodName", searchRequest.criteria.get(1).attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, searchRequest.criteria.get(1).operator);
        Assertions.assertEquals("translate", searchRequest.criteria.get(1).value);

        Assertions.assertEquals(2, searchRequest.groups.size());
        Assertions.assertEquals("G1", searchRequest.groups.get(0).id);
        Assertions.assertEquals(Operator.AND_OPERATOR, searchRequest.groups.get(0).operator);
        Assertions.assertEquals("C1", searchRequest.groups.get(0).criteriaIds.get(0));

        Assertions.assertEquals("G2", searchRequest.groups.get(1).id);
        Assertions.assertEquals(Operator.AND_OPERATOR, searchRequest.groups.get(1).operator);
        Assertions.assertEquals("C2", searchRequest.groups.get(1).criteriaIds.get(0));
    }

    @Test
    void translateQuery_2() {
        SearchRequest searchRequest = SearchRequestQueryTranslator.translateQuery("""
                logLevel = 'ERROR' | (methodName = 'translate' & className != 'Main')
                """, LogEntity.class);

//        FINAL_GROUP (OR)
// └── G5 (OR)
//     ├── G1 (logLevel = 'ERROR')
//     └── G4 (AND)
//         ├── G2 (methodName = 'translate')
//         └── G3 (className != 'Main')

        Assertions.assertNotNull(searchRequest);
        Assertions.assertEquals(3, searchRequest.criteria.size());

        Assertions.assertEquals("C1", searchRequest.criteria.get(0).id);
        Assertions.assertEquals("logLevel", searchRequest.criteria.get(0).attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, searchRequest.criteria.get(0).operator);
        Assertions.assertEquals("ERROR", searchRequest.criteria.get(0).value);

        Assertions.assertEquals("C2", searchRequest.criteria.get(1).id);
        Assertions.assertEquals("methodName", searchRequest.criteria.get(1).attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, searchRequest.criteria.get(1).operator);
        Assertions.assertEquals("translate", searchRequest.criteria.get(1).value);

        Assertions.assertEquals("C3", searchRequest.criteria.get(2).id);
        Assertions.assertEquals("className", searchRequest.criteria.get(2).attr);
        Assertions.assertEquals(SearchOperation.NOT_EQUALS_OPERATION, searchRequest.criteria.get(2).operator);
        Assertions.assertEquals("Main", searchRequest.criteria.get(2).value);

        Assertions.assertEquals(3, searchRequest.groups.size());

        Assertions.assertEquals("G1", searchRequest.groups.get(0).id);
        Assertions.assertEquals(Operator.AND_OPERATOR, searchRequest.groups.get(0).operator);
        Assertions.assertEquals(List.of("C1"), searchRequest.groups.get(0).criteriaIds);

        Assertions.assertEquals("G2", searchRequest.groups.get(1).id);
        Assertions.assertEquals(Operator.AND_OPERATOR, searchRequest.groups.get(1).operator);
        Assertions.assertEquals(List.of("C2"), searchRequest.groups.get(1).criteriaIds);

        Assertions.assertEquals(true, searchRequest.mergedGroups.containsKey(SearchRequest.FINAL_GROUP_CONSTANT));
        Assertions.assertEquals(true, searchRequest.mergedGroups.get(SearchRequest.FINAL_GROUP_CONSTANT).containsKey(Operator.OR_OPERATOR));
        Assertions.assertEquals(2, searchRequest.mergedGroups.get(SearchRequest.FINAL_GROUP_CONSTANT).get(Operator.OR_OPERATOR).size());
    }

    @Test
    void translateQuery_3() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery("""
                    (logLevel = 'ERROR' & methodName = 'validate') | (methodName = 'translate' & (className = 'Main' | className = 'Validator'))
                """, LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(5, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("C1", c1.id);
        Assertions.assertEquals("logLevel", c1.attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, c1.operator);
        Assertions.assertEquals("ERROR", c1.value);

        Criterion c2 = request.criteria.get(1);
        Assertions.assertEquals("C2", c2.id);
        Assertions.assertEquals("methodName", c2.attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, c2.operator);
        Assertions.assertEquals("validate", c2.value);

        Criterion c3 = request.criteria.get(2);
        Assertions.assertEquals("C3", c3.id);
        Assertions.assertEquals("methodName", c3.attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, c3.operator);
        Assertions.assertEquals("translate", c3.value);

        Criterion c4 = request.criteria.get(3);
        Assertions.assertEquals("C4", c4.id);
        Assertions.assertEquals("className", c4.attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, c4.operator);
        Assertions.assertEquals("Main", c4.value);

        Criterion c5 = request.criteria.get(4);
        Assertions.assertEquals("C5", c5.id);
        Assertions.assertEquals("className", c5.attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, c5.operator);
        Assertions.assertEquals("Validator", c5.value);

        Assertions.assertEquals(5, request.groups.size());

        // G1: logLevel = 'ERROR'
        var g1 = request.groups.stream().filter(g -> g.id.equals("G1")).findFirst().orElseThrow();
        Assertions.assertEquals(List.of("C1"), g1.criteriaIds);
        Assertions.assertEquals(Operator.AND_OPERATOR, g1.operator);

        // G2: methodName = 'validate'
        var g2 = request.groups.stream().filter(g -> g.id.equals("G2")).findFirst().orElseThrow();
        Assertions.assertEquals(List.of("C2"), g2.criteriaIds);
        Assertions.assertEquals(Operator.AND_OPERATOR, g2.operator);

        // G3 = G1 AND G2
        Assertions.assertTrue(request.mergedGroups.containsKey("G3"));
        var g3OpMap = request.mergedGroups.get("G3");
        Assertions.assertTrue(g3OpMap.containsKey(Operator.AND_OPERATOR));
        Assertions.assertEquals(List.of("G1", "G2"), g3OpMap.get(Operator.AND_OPERATOR));

        // G4: methodName = 'translate'
        var g4 = request.groups.stream().filter(g -> g.id.equals("G4")).findFirst().orElseThrow();
        Assertions.assertEquals(List.of("C3"), g4.criteriaIds);
        Assertions.assertEquals(Operator.AND_OPERATOR, g4.operator);

        // G5: className = 'Main'
        var g5 = request.groups.stream().filter(g -> g.id.equals("G5")).findFirst().orElseThrow();
        Assertions.assertEquals(List.of("C4"), g5.criteriaIds);
        Assertions.assertEquals(Operator.AND_OPERATOR, g5.operator);

        // G6: className = 'Validator'
        var g6 = request.groups.stream().filter(g -> g.id.equals("G6")).findFirst().orElseThrow();
        Assertions.assertEquals(List.of("C5"), g6.criteriaIds);
        Assertions.assertEquals(Operator.AND_OPERATOR, g6.operator);

        // G7 = G5 OR G6
        Assertions.assertTrue(request.mergedGroups.containsKey("G7"));
        var g7OpMap = request.mergedGroups.get("G7");
        Assertions.assertTrue(g7OpMap.containsKey(Operator.OR_OPERATOR));
        Assertions.assertEquals(List.of("G5", "G6"), g7OpMap.get(Operator.OR_OPERATOR));

        // G8 = G4 AND G7
        Assertions.assertTrue(request.mergedGroups.containsKey("G8"));
        var g8OpMap = request.mergedGroups.get("G8");
        Assertions.assertTrue(g8OpMap.containsKey(Operator.AND_OPERATOR));
        Assertions.assertEquals(List.of("G4", "G7"), g8OpMap.get(Operator.AND_OPERATOR));

        // FINAL_GROUP = G3 OR G8
        Assertions.assertTrue(request.mergedGroups.containsKey(SearchRequest.FINAL_GROUP_CONSTANT));
        var finalGroupOpMap = request.mergedGroups.get(SearchRequest.FINAL_GROUP_CONSTANT);
        Assertions.assertTrue(finalGroupOpMap.containsKey(Operator.OR_OPERATOR));
        Assertions.assertEquals(List.of("G3", "G8"), finalGroupOpMap.get(Operator.OR_OPERATOR));
    }

    @Test
    void translateQuery_likeOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "methodName ~ 'test'", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("methodName", c1.attr);
        Assertions.assertEquals(SearchOperation.LIKE_OPERATION, c1.operator);
        Assertions.assertEquals("%test%", c1.value);
    }

    @Test
    void translateQuery_notLikeOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "methodName !~ 'spam'", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("methodName", c1.attr);
        Assertions.assertEquals(SearchOperation.NOT_LIKE_OPERATION, c1.operator);
        Assertions.assertEquals("%spam%", c1.value);
    }

    @Test
    void translateQuery_greaterThanOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "lineNumber > 100", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("lineNumber", c1.attr);
        Assertions.assertEquals(SearchOperation.GREATER_OPERATION, c1.operator);
        Assertions.assertEquals(100L, c1.value);
    }

    @Test
    void translateQuery_lessThanOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "lineNumber < 50", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("lineNumber", c1.attr);
        Assertions.assertEquals(SearchOperation.LESS_OPERATION, c1.operator);
        Assertions.assertEquals(50L, c1.value);
    }

    @Test
    void translateQuery_greaterEqualOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "lineNumber >= 100", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("lineNumber", c1.attr);
        Assertions.assertEquals(SearchOperation.GREATER_EQUAL_OPERATION, c1.operator);
        Assertions.assertEquals(100L, c1.value);
    }

    @Test
    void translateQuery_lessEqualOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "lineNumber <= 200", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("lineNumber", c1.attr);
        Assertions.assertEquals(SearchOperation.LESS_EQUAL_OPERATION, c1.operator);
        Assertions.assertEquals(200L, c1.value);
    }

    @Test
    void translateQuery_isNullOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "errorMessage IS NULL", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("errorMessage", c1.attr);
        Assertions.assertEquals(SearchOperation.IS_NULL_OPERATION, c1.operator);
        Assertions.assertNull(c1.value);
    }

    @Test
    void translateQuery_isNotNullOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "errorMessage IS NOT NULL", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("errorMessage", c1.attr);
        Assertions.assertEquals(SearchOperation.IS_NOT_NULL_OPERATION, c1.operator);
        Assertions.assertNull(c1.value);
    }

    @Test
    void translateQuery_inOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "logLevel IN ('ERROR', 'WARN', 'INFO')", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("logLevel", c1.attr);
        Assertions.assertEquals(SearchOperation.IN_OPERATION, c1.operator);
        Assertions.assertTrue(c1.value instanceof List);
        List<?> values = (List<?>) c1.value;
        Assertions.assertEquals(3, values.size());
        Assertions.assertEquals("ERROR", values.get(0));
        Assertions.assertEquals("WARN", values.get(1));
        Assertions.assertEquals("INFO", values.get(2));
    }

    @Test
    void translateQuery_notInOperation() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "logLevel NOT IN ('DEBUG', 'TRACE')", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("logLevel", c1.attr);
        Assertions.assertEquals(SearchOperation.NOT_IN_OPERATION, c1.operator);
        Assertions.assertTrue(c1.value instanceof List);
        List<?> values = (List<?>) c1.value;
        Assertions.assertEquals(2, values.size());
        Assertions.assertEquals("DEBUG", values.get(0));
        Assertions.assertEquals("TRACE", values.get(1));
    }

    @Test
    void translateQuery_complexWithMultipleOperators() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "(logLevel = 'ERROR' | logLevel = 'WARN') & methodName ~ 'process' & lineNumber >= 50", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(4, request.criteria.size());

        // Check LIKE operation
        Criterion likeCriterion = request.criteria.stream()
                .filter(c -> c.operator == SearchOperation.LIKE_OPERATION)
                .findFirst().orElseThrow();
        Assertions.assertEquals("methodName", likeCriterion.attr);
        Assertions.assertEquals("%process%", likeCriterion.value);

        // Check GREATER_EQUAL operation
        Criterion geCriterion = request.criteria.stream()
                .filter(c -> c.operator == SearchOperation.GREATER_EQUAL_OPERATION)
                .findFirst().orElseThrow();
        Assertions.assertEquals("lineNumber", geCriterion.attr);
        Assertions.assertEquals(50L, geCriterion.value);
    }

    @Test
    void translateQuery_booleanValue() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "deleted = false", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("deleted", c1.attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, c1.operator);
        Assertions.assertEquals(false, c1.value);
    }

    @Test
    void translateQuery_numericValueWithDecimal() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "percentage >= 99.5", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("percentage", c1.attr);
        Assertions.assertEquals(SearchOperation.GREATER_EQUAL_OPERATION, c1.operator);
        Assertions.assertEquals(99.5, c1.value);
    }

    @Test
    void translateQuery_invalidSyntax_throwsException() {
        Assertions.assertThrows(SearchRequestQueryTranslator.QuerySyntaxException.class, () -> {
            SearchRequestQueryTranslator.translateQuery("invalid query without operator", LogEntity.class);
        });
    }

    @Test
    void translateQuery_emptyQuery_throwsException() {
        Assertions.assertThrows(SearchRequestQueryTranslator.QuerySyntaxException.class, () -> {
            SearchRequestQueryTranslator.translateQuery("", LogEntity.class);
        });
    }

    @Test
    void translateQuery_unbalancedParentheses_throwsException() {
        Assertions.assertThrows(SearchRequestQueryTranslator.QuerySyntaxException.class, () -> {
            SearchRequestQueryTranslator.translateQuery("(logLevel = 'ERROR'", LogEntity.class);
        });
    }

    @Test
    void validateQuery_validQuery() {
        SearchRequestQueryTranslator.ValidationResult result =
                SearchRequestQueryTranslator.validateQuery("logLevel = 'ERROR'", LogEntity.class);
        Assertions.assertTrue(result.valid);
    }

    @Test
    void validateQuery_invalidQuery() {
        SearchRequestQueryTranslator.ValidationResult result =
                SearchRequestQueryTranslator.validateQuery("invalid without operator", LogEntity.class);
        Assertions.assertFalse(result.valid);
        Assertions.assertNotNull(result.message);
    }

    @Test
    void getAvailableFields_returnsFields() {
        List<String> fields = SearchRequestQueryTranslator.getAvailableFields(LogEntity.class);
        Assertions.assertNotNull(fields);
        Assertions.assertTrue(fields.size() > 0);
        Assertions.assertTrue(fields.contains("logLevel"));
    }

    @Test
    void getSupportedOperators_returnsOperators() {
        List<SearchRequestQueryTranslator.OperatorInfo> operators =
                SearchRequestQueryTranslator.getSupportedOperators();
        Assertions.assertNotNull(operators);
        Assertions.assertTrue(operators.size() >= 8);

        // Check some operators exist
        Assertions.assertTrue(operators.stream().anyMatch(o -> o.symbol.equals("=")));
        Assertions.assertTrue(operators.stream().anyMatch(o -> o.symbol.equals("~")));
        Assertions.assertTrue(operators.stream().anyMatch(o -> o.symbol.equals(">")));
    }

    @Test
    void translateQuery_valueWithSpaces() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "methodName = 'process data'", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("process data", c1.value);
    }

    @Test
    void translateQuery_doubleQuotes() {
        SearchRequest request = SearchRequestQueryTranslator.translateQuery(
                "methodName = \"test value\"", LogEntity.class);

        Assertions.assertNotNull(request);
        Assertions.assertEquals(1, request.criteria.size());

        Criterion c1 = request.criteria.get(0);
        Assertions.assertEquals("test value", c1.value);
    }
}
