package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.logging.LogEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchRequestQueryTranslatorTest {

    @Test
    void translateQuery_1() {
        SearchRequest searchRequest = SearchRequestQueryTranslator.translateQuery("""
                logLevel = 'ERROR' & methodName = 'translate'
                """, LogEntity.class);

//        SearchRequest request = new SearchRequest();
//        Criterion criterion1 = new Criterion("C1", LogEntity.class.getTypeName(), "logLevel", SearchOperation.EQUALS_OPERATION, "ERROR");
//        Criterion criterion2 = new Criterion("C1", LogEntity.class.getTypeName(), "methodName", SearchOperation.EQUALS_OPERATION, "translate");
//        request.addCriterion("G1", Operator.AND_OPERATOR, criterion1, criterion2);

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

        Assertions.assertEquals(1, searchRequest.groups.size());
        Assertions.assertEquals("G1", searchRequest.groups.get(0).id);
        Assertions.assertEquals(Operator.AND_OPERATOR, searchRequest.groups.get(0).operator);
        Assertions.assertEquals("C1", searchRequest.groups.get(0).criteriaIds.get(0));
        Assertions.assertEquals("C2", searchRequest.groups.get(0).criteriaIds.get(1));
    }
}