package com.bervan.common.onevalue;

import com.bervan.history.model.BaseRepository;

import java.util.Optional;
import java.util.UUID;

public interface OneValueRepository extends BaseRepository<OneValue, UUID> {
    Optional<OneValue> findByNameAndOwnerId(String name, UUID ownerId);
}
