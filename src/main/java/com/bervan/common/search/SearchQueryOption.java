package com.bervan.common.search;

import com.bervan.common.search.model.SortDirection;
import jakarta.validation.constraints.NotNull;

public class SearchQueryOption {
    @NotNull
    private SortDirection sortDirection;
    @NotNull
    private String sortField;
    @NotNull
    private Integer page;
    @NotNull
    private Integer pageSize;
    private String entityToFind;

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

    public String getEntityToFind() {
        return entityToFind;
    }

    public void setEntityToFind(String entityToFind) {
        this.entityToFind = entityToFind;
    }
}
