package com.bervan.asynctask;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.logging.JsonLogger;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HistoryAsyncTaskService extends BaseService<UUID, HistoryAsyncTask> {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

    protected HistoryAsyncTaskService(BaseRepository<HistoryAsyncTask, UUID> repository, SearchService searchService) {
        super(repository, searchService);
    }
}
