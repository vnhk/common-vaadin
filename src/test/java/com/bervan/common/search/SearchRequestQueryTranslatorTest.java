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
}