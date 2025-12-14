package com.bervan.common.model;

import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

@MappedSuperclass
public abstract class BervanBaseEntity<ID extends Serializable> implements AbstractBaseEntity<ID>, PersistableData<ID> {

}
