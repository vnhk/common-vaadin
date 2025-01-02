package com.bervan.common.search.model;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.history.model.AbstractBaseEntity;

import java.util.Map;

public class SearchCriteriaHolder {
    private Class<? extends BervanBaseEntity> entityToFind;
    private QueryHolder holder;
    private Map<QueryMapping, SearchCriteria> searchCriteria;

    public Class<? extends BervanBaseEntity> getEntityToFind() {
        return entityToFind;
    }

    public void setEntityToFind(Class<? extends BervanBaseEntity> entityToFind) {
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
