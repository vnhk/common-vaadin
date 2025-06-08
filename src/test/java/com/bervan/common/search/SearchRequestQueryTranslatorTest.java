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

//
//        FINAL_GROUP (OR)
//├── G3 (AND)
//│   ├── G1 → C1: logLevel = 'ERROR'
//│   └── G2 → C2: methodName = 'validate'
//└── G8 (AND)
//    ├── G4 → C3: methodName = 'translate'
//    └── G7 (OR)
//        ├── G5 → C4: className = 'Main'
//        └── G6 → C5: className = 'Validator'

        Assertions.assertNotNull(request);
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

        // G1: C1 AND C2
        var g1 = request.groups.get(0);
        Assertions.assertEquals("G1", g1.id);
        Assertions.assertEquals(Operator.AND_OPERATOR, g1.operator);
        Assertions.assertEquals(List.of("C1", "C2"), g1.criteriaIds);

        // G3: C4 OR C5
        var g3 = request.groups.get(1);
        Assertions.assertEquals("G3", g3.id);
        Assertions.assertEquals(Operator.OR_OPERATOR, g3.operator);
        Assertions.assertEquals(List.of("C4", "C5"), g3.criteriaIds);

        // G4: C3 AND G3
        var g4 = request.groups.get(2);
        Assertions.assertEquals("G4", g4.id);
        Assertions.assertEquals(Operator.AND_OPERATOR, g4.operator);
        Assertions.assertEquals(List.of("C3"), g4.criteriaIds);

        // G5: G1 OR G4
        var g5 = request.groups.get(3);
        Assertions.assertEquals("G5", g5.id);
        Assertions.assertEquals(Operator.OR_OPERATOR, g5.operator);

        // FINAL_GROUP: alias G5
        var finalGroup = request.groups.get(4);
        Assertions.assertEquals(SearchRequest.FINAL_GROUP_CONSTANT, finalGroup.id);
        Assertions.assertEquals(Operator.OR_OPERATOR, finalGroup.operator);
    }
}