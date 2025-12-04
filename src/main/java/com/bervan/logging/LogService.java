package com.bervan.logging;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class LogService extends BaseService<Long, LogEntity> {

    public LogService(LogRepository logRepository, SearchService searchService) {
        super(logRepository, searchService);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2); //change to 7 once server be better
        ((LogRepository) repository).deleteOwnersByOldLogs(cutoff);
        ((LogRepository) repository).deleteOldLogs(cutoff);
    }

    public Set<String> loadAppsName() {
        return ((LogRepository) repository).findAllApplicationNames();
    }

    public List<String> loadProcessNames() {
        return ((LogRepository) repository).findAllProcessNames();
    }

    public List<String> loadModulesNames() {
        return ((LogRepository) repository).findAllModulesNames();
    }
}