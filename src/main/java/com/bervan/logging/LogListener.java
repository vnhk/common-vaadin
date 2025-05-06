package com.bervan.logging;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.bervan.logging.LogEntity.MAX_LOG_MESSAGE_LENGTH;

@Service
@Profile("!test && !it && !debug")
public class LogListener {

    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final User commonUser;

    public LogListener(LogRepository logRepository, UserRepository userRepository) {
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        commonUser = userRepository.findByUsername("COMMON_USER").get();

    }

    @RabbitListener(queues = "LOGS_QUEUE", concurrency = "1")
    public void receiveLog(LogMessage logMessage) {
        try {
            LogEntity entity = new LogEntity();
            entity.setApplicationName(logMessage.getApplicationName());
            entity.setLogLevel(logMessage.getLogLevel());
            entity.setMessage(logMessage.getMessage());
            entity.setTimestamp(logMessage.getTimestamp());
            entity.setClassName(logMessage.getClassName());
            entity.setLineNumber(logMessage.getLineNumber());
            entity.setMethodName(logMessage.getMethodName());

            if (entity.getMessage().length() > MAX_LOG_MESSAGE_LENGTH) {
                entity.setMessage(truncateLogMessage(entity.getMessage()));
            }

            entity.addOwner(commonUser);
            logRepository.save(entity);
        } catch (Throwable e) {
            //don't do anything, we don't want to have infinite loop of logs
        }

    }

    private String truncateLogMessage(String logMessage) {
        if (logMessage.length() > MAX_LOG_MESSAGE_LENGTH - 25) {
            return "TOO LONG LOG: " + logMessage.substring(0, MAX_LOG_MESSAGE_LENGTH - 25);
        }
        return logMessage;
    }

}
