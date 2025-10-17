package com.bervan.asynctask;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.history.model.HistoryCollection;
import com.bervan.history.model.HistorySupported;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@HistorySupported
public class AsyncTask extends BervanBaseEntity<UUID> implements PersistableTableData<UUID> {
    @Id
    private UUID id;
    private String status;
    private String message;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean notified = false;
    private Boolean notifyOnSuccess = true;
    private Integer timeoutInMin = 60;
    private Boolean deleted = false;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @HistoryCollection(historyClass = HistoryAsyncTask.class)
    private Set<HistoryAsyncTask> history = new HashSet<>();

    @Override
    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID uuid) {
        this.id = uuid;
    }

    @Override
    public String getTableFilterableColumnValue() {
        return id.toString();
    }

    @Override
    public Boolean isDeleted() {
        return deleted;
    }
}
