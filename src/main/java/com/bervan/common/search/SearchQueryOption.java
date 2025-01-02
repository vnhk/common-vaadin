package com.bervan.common.search;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.search.model.SortDirection;
import jakarta.validation.constraints.NotNull;

public class SearchQueryOption {
    @NotNull
    private SortDirection sortDirection = SortDirection.ASC;
    @NotNull
    private String sortField = "id";
    @NotNull
    private Integer page = 0;
    @NotNull
    private Integer pageSize = 50;
    private Class<? extends BervanBaseEntity> entityToFind;

    public SearchQueryOption() {

    }

    public SearchQueryOption(Class<? extends BervanBaseEntity> entityToFind) {
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

    public Class<? extends BervanBaseEntity> getEntityToFind() {
        return entityToFind;
    }

    public void setEntityToFind(Class<? extends BervanBaseEntity> entityToFind) {
        this.entityToFind = entityToFind;
    }
}
