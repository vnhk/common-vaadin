package com.bervan.common.onevalue;

import com.bervan.history.model.BaseRepository;

import java.util.List;
import java.util.UUID;

public interface OneValueRepository extends BaseRepository<OneValue, UUID> {
    List<OneValue> findByNameAndOwnersId(String name, UUID ownerId);
}
