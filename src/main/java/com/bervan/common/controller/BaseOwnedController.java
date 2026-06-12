package com.bervan.common.controller;

import com.bervan.common.config.EntityConfigValidator;
import com.bervan.common.mapper.BervanDTOMapper;
import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BaseModel;

import java.io.Serializable;

public abstract class BaseOwnedController<T extends BervanOwnedBaseEntity<ID> & BaseModel<ID>, ID extends Serializable> extends BaseController<T, ID> {

    protected BaseOwnedController(BaseService<ID, T> service, BervanDTOMapper mapper, EntityConfigValidator validator, String entityName) {
        super(service, mapper, validator, entityName);
    }
}
