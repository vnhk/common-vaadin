package com.bervan.common.model;

public interface BervanLogger {
    void logError(String message);

    void logInfo(String message);

    void logDebug(String message);

    void logWarn(String message);

    void logError(String message, Throwable throwable);

    void logWarn(String message, Throwable throwable);

    void logError(Throwable throwable);

    void logWarn(Throwable throwable);
}
