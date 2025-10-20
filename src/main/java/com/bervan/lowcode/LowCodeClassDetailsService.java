package com.bervan.lowcode;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LowCodeClassDetailsService extends BaseService<UUID, LowCodeClassDetails> {
    protected LowCodeClassDetailsService(BaseRepository<LowCodeClassDetails, UUID> repository, SearchService searchService) {
        super(repository, searchService);
    }
}
