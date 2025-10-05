package com.bervan.asynctask;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class HistoryAsyncTaskService extends BaseService<UUID, HistoryAsyncTask> {

    protected HistoryAsyncTaskService(BaseRepository<HistoryAsyncTask, UUID> repository, SearchService searchService) {
        super(repository, searchService);
    }
}
