package com.bervan.common.config;

import com.bervan.logging.JsonLogger;

public class MemoryChecker {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

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
