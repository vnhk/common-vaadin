package com.bervan.common.search.model;


public class QueryMapping {
    private final String query;
    private final String code;


    public QueryMapping(String query, String code) {
        this.query = query;
        this.code = code;
    }

    public String getQuery() {
        return query;
    }

    public String getCode() {
        return code;
    }
}
