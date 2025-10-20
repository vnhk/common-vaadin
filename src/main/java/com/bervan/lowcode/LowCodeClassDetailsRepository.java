package com.bervan.lowcode;

import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LowCodeClassDetailsRepository extends BaseRepository<LowCodeClassDetails, UUID> {
}
