package com.bervan.common;

import com.bervan.core.model.BervanLogger;
import org.slf4j.Logger;

public class BervanLoggerImpl implements BervanLogger {
    Logger logger;

    private BervanLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    public static BervanLoggerImpl init(Logger logger) {
        return new BervanLoggerImpl(logger);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    @Override
    public void error(Throwable throwable) {
        logger.error("Error:", throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        logger.warn("Error:", throwable);
    }
}
