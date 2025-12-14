package com.bervan.common.model;

import com.bervan.history.model.AbstractBaseHistoryEntity;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

@MappedSuperclass
public abstract class BervanHistoryEntity<ID extends Serializable> implements AbstractBaseHistoryEntity<ID>, PersistableData<ID> {

    @Override
    public void setDeleted(Boolean value) {

    }
}
