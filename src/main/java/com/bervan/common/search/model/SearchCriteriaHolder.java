package com.bervan.common.search.model;

import com.bervan.history.model.AbstractBaseEntity;

import java.util.Map;

public class SearchCriteriaHolder {
    private Class<? extends AbstractBaseEntity> entityToFind;
    private QueryHolder holder;
    private Map<QueryMapping, SearchCriteria> searchCriteria;

    public Class<? extends AbstractBaseEntity> getEntityToFind() {
        return entityToFind;
    }

    public void setEntityToFind(Class<? extends AbstractBaseEntity> entityToFind) {
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
