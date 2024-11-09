package com.bervan.common.user;

import com.bervan.history.model.BaseRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
