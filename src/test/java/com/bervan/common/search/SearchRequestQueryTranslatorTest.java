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
        Assertions.assertEquals(LogEntity.class.getTypeName(), searchRequest.criteria.get(0).type);
        Assertions.assertEquals("logLevel", searchRequest.criteria.get(0).attr);
        Assertions.assertEquals(SearchOperation.EQUALS_OPERATION, searchRequest.criteria.get(0).operator);
        Assertions.assertEquals("ERROR", searchRequest.criteria.get(0).value);

        Assertions.assertEquals("C2", searchRequest.criteria.get(1).id);
        Assertions.assertEquals(LogEntity.class.getTypeName(), searchRequest.criteria.get(1).type);
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

//        SearchRequest request = new SearchRequest();
//        Criterion criterion1 = new Criterion("C1", LogEntity.class.getTypeName(), "logLevel", SearchOperation.EQUALS_OPERATION, "ERROR");
//        Criterion criterion2 = new Criterion("C2", LogEntity.class.getTypeName(), "methodName", SearchOperation.EQUALS_OPERATION, "translate");
//        Criterion criterion3 = new Criterion("C3", LogEntity.class.getTypeName(), "className", SearchOperation.NOT_EQUALS_OPERATION, "Main");
//        request.addCriterion("G1", Operator.AND_OPERATOR, criterion1);
//        request.addCriterion("G2", Operator.OR_OPERATOR, criterion2, criterion3);
//        request.mergeGroup(SearchRequest.FINAL_GROUP_CONSTANT, Operator.OR_OPERATOR, "G1", "G2");
        //mergeGroup(String newGroup, Operator operator, String... groups) {

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
        Assertions.assertEquals(1, searchRequest.mergedGroups.get(SearchRequest.FINAL_GROUP_CONSTANT).get(Operator.OR_OPERATOR).size());
    }
}