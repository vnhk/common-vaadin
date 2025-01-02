package com.bervan.common.search.model;

import com.bervan.history.model.Persistable;

import java.util.List;

public class SearchResponse<T extends Persistable> {

    public SearchResponse(List<T> resultList, Integer currentFound, Integer currentPage, Long allFound) {
        this.resultList = resultList;
        this.currentFound = currentFound;
        this.currentPage = currentPage;
        this.allFound = allFound;
    }

    private List<T> resultList;
    private Integer currentFound;
    private Integer currentPage;
    private Long allFound;

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
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

    public Long getAllFound() {
        return allFound;
    }

    public void setAllFound(Long allFound) {
        this.allFound = allFound;
    }
}
