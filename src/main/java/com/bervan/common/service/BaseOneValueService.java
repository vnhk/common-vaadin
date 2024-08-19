package com.bervan.common.service;

import com.bervan.common.model.BaseOneValue;

import java.util.Optional;

public interface BaseOneValueService<T extends BaseOneValue> {
    Optional<T> loadByKey(String key);

    void save(T item);
}
