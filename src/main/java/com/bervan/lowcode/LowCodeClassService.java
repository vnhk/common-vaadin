package com.bervan.lowcode;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LowCodeClassService extends BaseService<UUID, LowCodeClass> {
    protected LowCodeClassService(BaseRepository<LowCodeClass, UUID> repository, SearchService searchService) {
        super(repository, searchService);
    }

    public void generateCode(LowCodeClass toSet) {
        //Spring profile not prod:
        System.out.printf("Generating code for %s not implemented yet!!!!");
    }
}
