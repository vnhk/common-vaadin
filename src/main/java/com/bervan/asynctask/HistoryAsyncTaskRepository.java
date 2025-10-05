package com.bervan.asynctask;

import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryAsyncTaskRepository extends BaseRepository<HistoryAsyncTask, UUID> {
}
