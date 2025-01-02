package com.bervan.common.user;

import com.bervan.history.model.BaseRepository;

import java.util.List;
import java.util.UUID;

public interface UserToUserRelationRepository extends BaseRepository<UserToUserRelation, UUID> {
    List<UserToUserRelation> findAllByParentUsername(String username);
}
