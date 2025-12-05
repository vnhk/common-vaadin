package com.bervan.logging;

import ch.qos.logback.classic.Logger;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Map;


public class JsonLogger {
    private final Logger logger;
    private String moduleName;

    private JsonLogger(Class<?> cl, String moduleName) {
        this.logger = (Logger) LoggerFactory.getLogger(cl);
        this.moduleName = moduleName;
    }

    public static JsonLogger getLogger(Class<?> cl, String moduleName) {
        return new JsonLogger(cl, moduleName);
    }

    public void error(String message, Throwable e) {
        log(Level.ERROR, message, null, e);
    }

    public void debug(String message, Throwable e) {
        log(Level.DEBUG, message, null, e);
    }

    public void info(Map.Entry<String, Map<String, Object>> json, String message, Object... params) {
        log(Level.INFO, message, createParamsWithJson(json, params), null);
    }

    public void error(Map.Entry<String, Map<String, Object>> json, String message, Object... params) {
        log(Level.ERROR, message, createParamsWithJson(json, params), null);
    }

    public void error(String message, Object... params) {
        log(Level.ERROR, message, params, null);
    }

    public void info(String message, Object... params) {
        log(Level.INFO, message, params, null);
    }

    public void debug(String message, Object... params) {
        log(Level.DEBUG, message, params, null);
    }

    public void trace(String message, Object... params) {
        log(Level.TRACE, message, params, null);
    }

    public void warn(String message, Object... params) {
        log(Level.WARN, message, params, null);
    }

    public void debug(Map.Entry<String, Map<String, Object>> json, String message, Object... params) {
        log(Level.DEBUG, message, createParamsWithJson(json, params), null);
    }

    public void trace(Map.Entry<String, Map<String, Object>> json, String message, Object... params) {
        log(Level.TRACE, message, createParamsWithJson(json, params), null);
    }

    public void warn(Map.Entry<String, Map<String, Object>> json, String message, Object... params) {
        log(Level.WARN, message, createParamsWithJson(json, params), null);
    }

    public void error(Map.Entry<String, Map<String, Object>> json, Throwable throwable) {
        log(Level.ERROR, null, createParamsWithJson(json, null), throwable);
    }

    public void info(String message) {
        log(Level.INFO, message, null, null);
    }

    public void debug(String message) {
        log(Level.DEBUG, message, null, null);
    }

    public void trace(String message) {
        log(Level.TRACE, message, null, null);
    }

    public void warn(String message) {
        log(Level.WARN, message, null, null);
    }

    public void error(String message) {
        log(Level.ERROR, message, null, null);
    }

    public void error(Throwable throwable) {
        log(Level.ERROR, null, null, throwable);
    }

    private Object[] createParamsWithJson(Map.Entry<String, Map<String, Object>> json, Object[] params) {
        StructuredArgument structuredArgument = StructuredArguments.keyValue(json.getKey(), json.getValue());
        if (params == null || params.length == 0) {
            return new Object[]{structuredArgument};
        } else {
            Object[] newParams = new Object[params.length + 1];
            System.arraycopy(params, 0, newParams, 0, params.length);
            newParams[params.length] = structuredArgument;
            return newParams;
        }
    }

    private void log(Level level, String message, Object[] params, Throwable throwable) {
        if (!logger.isEnabledForLevel(level)) {
            return;
        }

        if (params == null || params.length == 0) {
            //append module name as new field in json
            StructuredArgument structuredArgument = StructuredArguments.keyValue("moduleName", moduleName);
            params = new Object[1];
            System.arraycopy(params, 0, params, 1, 0);
            params[0] = structuredArgument;
        } else {
            Object[] newParams = new Object[params.length + 1];
            System.arraycopy(params, 0, newParams, 0, params.length);
            newParams[params.length] = StructuredArguments.keyValue("moduleName", moduleName);
            params = newParams;
        }

        try {
            logger.log(null, this.getClass().getName(), level.toInt(), message, params, throwable);
        } catch (Exception e) {
            logger.error("Could not log message!", e);
        }
    }
}
