package com.bervan.logging;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(
        name = "logs",
        indexes = {
                @Index(name = "idx_application_name", columnList = "applicationName"),
                @Index(name = "idx_line_number", columnList = "lineNumber"),
                @Index(name = "idx_log_level", columnList = "logLevel"),
                @Index(name = "idx_class_name", columnList = "className"),
                @Index(name = "idx_method_name", columnList = "methodName"),
                @Index(name = "idx_timestamp", columnList = "timestamp")
        }
)
@Getter
@Setter
@AllArgsConstructor
public class LogEntity extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    public static final int MAX_LOG_MESSAGE_LENGTH = 20000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String applicationName;
    private int lineNumber;

    @VaadinTableColumn(internalName = "level", displayName = "Level",
            inEditForm = false, inSaveForm = false, strValues = {"DEBUG", "INFO", "WARN", "ERROR"})
    private String logLevel;

    private String className;

    private String methodName;

    @VaadinTableColumn(internalName = "timestamp", displayName = "Timestamp", inEditForm = false, inSaveForm = false, inTable = false)
    private LocalDateTime timestamp;

    @Size(max = MAX_LOG_MESSAGE_LENGTH)
    @Column(columnDefinition = "MEDIUMTEXT")
    private String message;

    @Transient
    @VaadinTableColumn(internalName = "log", displayName = "Log", inEditForm = false, inSaveForm = false)
    private String fullLog;

    public LogEntity() {
    }

    public String getFullLog() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss:SSS");
        return String.format("%s %s:%s:%d - %s", timestamp.format(formatter), className, methodName, lineNumber, message);
    }

    @Override
    public Boolean isDeleted() {
        return false;
    }

    @Override
    public void setDeleted(Boolean value) {

    }

    @Override
    public LocalDateTime getModificationDate() {
        return timestamp;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }

    @Override
    public String getTableFilterableColumnValue() {
        return applicationName + "_" + timestamp;
    }
}
