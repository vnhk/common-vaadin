package com.bervan.logging;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "logs")
@Getter
@Setter
@AllArgsConstructor
public class LogEntity extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    public static final int MAX_LOG_MESSAGE_LENGTH = 2000;

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

    @Column(length = MAX_LOG_MESSAGE_LENGTH)
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
