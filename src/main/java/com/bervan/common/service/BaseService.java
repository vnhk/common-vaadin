package com.bervan.common.service;


import com.bervan.common.model.PersistableTableData;

import java.util.List;
import java.util.Set;

public interface BaseService<T extends PersistableTableData> {
    void save(List<T> data);

    T save(T data);

    Set<T> load();

}
