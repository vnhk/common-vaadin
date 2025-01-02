package com.bervan.common.search;

import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;

import java.util.ArrayList;
import java.util.List;

public class Group {
    public String id;
    public final List<String> criteriaIds = new ArrayList<>();
    public Operator operator;

    public Group(String id, Operator operator) {
        this.id = id;
        this.operator = operator;
    }
}
