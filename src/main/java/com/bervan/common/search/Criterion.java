package com.bervan.common.search;

import com.bervan.common.search.model.SearchOperation;

public class Criterion {
    public String id;
    public String type;
    public String attr;
    public SearchOperation operator;
    public Object value;

    public Criterion() {

    }

    public Criterion(String id, String type, String attr, SearchOperation operator, Object value) {
        this.id = id;
        this.type = type;
        this.attr = attr;
        this.operator = operator;
        this.value = value;
    }
}
