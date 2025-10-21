package com.bervan.lowcode;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LowCodeClassDetailsService extends BaseService<UUID, LowCodeClassDetails> {
    protected LowCodeClassDetailsService(BaseRepository<LowCodeClassDetails, UUID> repository, SearchService searchService) {
        super(repository, searchService);
    }

    public List<LowCodeClassDetails> loadByParentId(LowCodeClass lowCodeClass) {
        return ((LowCodeClassDetailsRepository) repository).findByLowCodeClass(lowCodeClass);
    }
}
