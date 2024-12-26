package com.bervan.common.service;


import com.bervan.common.model.PersistableTableData;
import com.bervan.ieentities.ExcelIEEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface BaseService<ID extends Serializable, T extends PersistableTableData<ID>> {
    void save(List<T> data);

    T save(T data);

    Set<T> load();

    void delete(T item);

    void saveIfValid(List<? extends ExcelIEEntity> objects);
}
