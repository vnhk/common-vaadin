package com.bervan.common.service;


import com.bervan.common.model.PersistableTableData;

import java.io.Serializable;

public interface CSVService<ID extends Serializable, T extends PersistableTableData<ID>> extends BaseService<ID, T> {

}
