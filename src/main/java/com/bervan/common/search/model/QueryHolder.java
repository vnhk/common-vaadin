package com.bervan.common.search.model;


import jakarta.persistence.criteria.Predicate;

public class QueryHolder {
    private Operator operator;
    private String code;
    private String value;
    private Object fstQuery;
    private Object sndQuery;
    private Predicate resultPredicate;

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getFstQuery() {
        return fstQuery;
    }

    public void setFstQuery(Object fstQuery) {
        this.fstQuery = fstQuery;
    }

    public Object getSndQuery() {
        return sndQuery;
    }

    public void setSndQuery(Object sndQuery) {
        this.sndQuery = sndQuery;
    }

    public Predicate getResultPredicate() {
        return resultPredicate;
    }

    public void setResultPredicate(Predicate resultPredicate) {
        this.resultPredicate = resultPredicate;
    }
}
