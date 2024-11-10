package com.bervan.common.service;


import com.bervan.common.model.PersistableTableData;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface BaseService<ID extends Serializable, T extends PersistableTableData<ID>> {
    void save(List<T> data);

    T save(T data);

    Set<T> load();

    void delete(T item);
}
