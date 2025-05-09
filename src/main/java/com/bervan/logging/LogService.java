package com.bervan.logging;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class LogService extends BaseService<Long, LogEntity> {

    public LogService(LogRepository logRepository, SearchService searchService) {
        super(logRepository, searchService);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1); //change to 7 once server be better
        ((LogRepository) repository).deleteOwnersByOldLogs(cutoff);
        ((LogRepository) repository).deleteOldLogs(cutoff);
    }

    public Set<String> loadAppsName() {
        return ((LogRepository) repository).findAllApplicationNames();
    }
}