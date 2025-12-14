package com.bervan.common.search.model;

import com.bervan.common.model.BervanOwnedBaseEntity;

import java.util.Map;

public class SearchCriteriaHolder {
    private Class<? extends BervanOwnedBaseEntity> entityToFind;
    private QueryHolder holder;
    private Map<QueryMapping, SearchCriteria> searchCriteria;

    public Class<? extends BervanOwnedBaseEntity> getEntityToFind() {
        return entityToFind;
    }

    public void setEntityToFind(Class<? extends BervanOwnedBaseEntity> entityToFind) {
        this.entityToFind = entityToFind;
    }

    public QueryHolder getHolder() {
        return holder;
    }

    public void setHolder(QueryHolder holder) {
        this.holder = holder;
    }

    public Map<QueryMapping, SearchCriteria> getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(Map<QueryMapping, SearchCriteria> searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
}
