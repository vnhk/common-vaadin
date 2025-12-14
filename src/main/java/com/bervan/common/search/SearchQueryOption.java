package com.bervan.common.search;

import com.bervan.common.search.model.SortDirection;
import com.bervan.history.model.AbstractBaseEntity;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SearchQueryOption {
    @NotNull
    private SortDirection sortDirection = SortDirection.ASC;
    @NotNull
    private String sortField = "id";
    @NotNull
    private Integer page = 0;
    @NotNull
    private Integer pageSize = 50;
    private Class<? extends AbstractBaseEntity> entityToFind;
    private List<String> columnsToFetch;
    private boolean countQuery;

    public SearchQueryOption() {

    }

    public SearchQueryOption(Class<? extends AbstractBaseEntity> entityToFind) {
        this.entityToFind = entityToFind;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Class<? extends AbstractBaseEntity> getEntityToFind() {
        return entityToFind;
    }

    public void setEntityToFind(Class<? extends AbstractBaseEntity> entityToFind) {
        this.entityToFind = entityToFind;
    }

    public void isCountQuery(boolean countQuery) {
        this.countQuery = countQuery;
    }

    public boolean isCountQuery() {
        return countQuery;
    }

    public List<String> getColumnsToFetch() {
        return columnsToFetch;
    }

    public void setColumnsToFetch(List<String> columnsToFetch) {
        this.columnsToFetch = columnsToFetch;
    }
}
