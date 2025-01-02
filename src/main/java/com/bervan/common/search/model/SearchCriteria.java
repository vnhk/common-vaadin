package com.bervan.common.search.model;


public class SearchCriteria {
    private String field;
    private SearchOperation operation;
    private Object value;

    public SearchCriteria() {

    }

    public SearchCriteria(String field, SearchOperation operation, Object value) {
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public void setOperation(SearchOperation operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
