package com.bervan.common.service;


import com.bervan.common.model.PersistableTableData;

import java.util.List;

public interface BaseService<T extends PersistableTableData> {
    void save(List<T> data);

    void save(T data);

    List<T> load();

}
