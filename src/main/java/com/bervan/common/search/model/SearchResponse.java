package com.bervan.common.search.model;

import com.bervan.history.model.Persistable;

import java.util.List;

public class SearchResponse {

    public SearchResponse(List<? extends Persistable> resultList, Integer currentFound, Integer currentPage, Integer allFound) {
        this.resultList = resultList;
        this.currentFound = currentFound;
        this.currentPage = currentPage;
        this.allFound = allFound;
    }

    private List<? extends Persistable> resultList;
    private Integer currentFound;
    private Integer currentPage;
    private Integer allFound;

    public List<? extends Persistable> getResultList() {
        return resultList;
    }

    public void setResultList(List<? extends Persistable> resultList) {
        this.resultList = resultList;
    }

    public Integer getCurrentFound() {
        return currentFound;
    }

    public void setCurrentFound(Integer currentFound) {
        this.currentFound = currentFound;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getAllFound() {
        return allFound;
    }

    public void setAllFound(Integer allFound) {
        this.allFound = allFound;
    }
}
