package com.bervan.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//@Service
@Slf4j
public class MemoryChecker {

//    @Scheduled(cron = "0 */5 * * * *")
    public void checkMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        log.debug("Total memory (bytes): " + totalMemory);
        log.debug("Free memory (bytes): " + freeMemory);
        log.debug("Max memory (bytes): " + maxMemory);

        log.debug("Total memory (MB): " + totalMemory / (1024 * 1024));
        log.debug("Free memory (MB): " + freeMemory / (1024 * 1024));
        log.debug("Max memory (MB): " + maxMemory / (1024 * 1024));
    }
}
