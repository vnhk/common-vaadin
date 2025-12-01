package com.bervan.logging;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
@Profile("!test && !it && !debug")
public class QueueAppender extends ConsoleAppender<ILoggingEvent> implements SmartLifecycle {
    private final JsonLogger log = JsonLogger.getLogger(getClass());

    private final RabbitTemplate rabbitTemplate;
    private final String applicationName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueueAppender(RabbitTemplate rabbitTemplate, @Value("${spring.application.name}") String applicationName) {
        this.rabbitTemplate = rabbitTemplate;
        this.applicationName = applicationName;
    }

    @Override
    public synchronized void doAppend(ILoggingEvent eventObject) {
        append(eventObject);
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (rabbitTemplate == null || applicationName == null) {
            return;
        }

        byte[] encodedMessage = encoder.encode(eventObject);
        String decodedString = new String(encodedMessage, StandardCharsets.UTF_8);

        Map<String, Object> json;
        String processName = null;
        String className = null;
        String method = null;
        String route = null;
        String packageName = null;
        try {
            json = objectMapper.readValue(decodedString, Map.class);
            packageName = getVal(json, "packageName");
            className = getVal(json, "class");
            method = getVal(json, "method");
            Object ctx = json.getOrDefault(BaseProcessContext.CTX, null);
            if (ctx instanceof Map) {
                processName = (String) ((Map) ctx).getOrDefault(BaseProcessContext.PROCESS_NAME, null);
                route = (String) ((Map) ctx).getOrDefault(BaseProcessContext.ROUTE, null);
            }
        } catch (Exception ignored) {
        }

        LogMessage logMessage;
        if (eventObject.getCallerData() != null && eventObject.getCallerData().length > 0) {
            StackTraceElement callerData = eventObject.getCallerData()[0];
            logMessage = new LogMessage(
                    applicationName,
                    eventObject.getLevel().toString(),
                    decodedString,
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(eventObject.getTimeStamp()),
                            ZoneId.systemDefault()
                    ),
                    packageName,
                    className,
                    method,
                    processName,
                    route,
                    callerData.getLineNumber(),
                    decodedString
            );
        } else {
            logMessage = new LogMessage(
                    applicationName,
                    eventObject.getLevel().toString(),
                    decodedString,
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(eventObject.getTimeStamp()),
                            ZoneId.systemDefault()
                    ),
                    null,
                    null,
                    null,
                    processName,
                    route,
                    -1,
                    decodedString
            );
        }

        try {
            rabbitTemplate.convertAndSend("LOGS_DIRECT_EXCHANGE", "LOGS_ROUTING_KEY", logMessage);
        } catch (Exception e) {
            addError("Failed to send log to RabbitMQ", e);
        }
    }

    private String getVal(Map<String, Object> json, String key) {
        Object orDefault = json.getOrDefault(key, null);
        if (orDefault == null) return null;
        String replaced = orDefault.toString().replace("?#?:?", "");
        if (replaced.isBlank()) {
            return null;
        }
        return replaced;
    }

    @Override
    public void start() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
//        encoder.setContext(loggerContext);
//        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n");
//        encoder.start();

        super.start();
        Logger rootLogger = loggerContext.getLogger("ROOT");
        AppenderDelegator<ILoggingEvent> delegate = (AppenderDelegator<ILoggingEvent>) rootLogger.getAppender("JSON_APPENDER");
        this.encoder = delegate.getEncoder();
        delegate.setDelegateAndReplayBuffer(this);
    }

    @Override
    public boolean isRunning() {
        return isStarted();
    }
}