package com.bervan.common.service;

import com.bervan.common.model.BaseOneValue;

import java.util.List;

public interface BaseOneValueService<T extends BaseOneValue> {
    List<T> loadByKey(String key);

    void save(T item);
}
