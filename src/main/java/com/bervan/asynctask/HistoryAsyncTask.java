package com.bervan.asynctask;

import com.bervan.common.model.BervanHistoryOwnedEntity;
import com.bervan.common.model.PersistableTableOwnedData;
import com.bervan.history.model.HistoryField;
import com.bervan.history.model.HistoryOwnerEntity;
import com.bervan.history.model.HistorySupported;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@HistorySupported
@Getter
@Setter
public class HistoryAsyncTask extends BervanHistoryOwnedEntity<UUID> implements PersistableTableOwnedData<UUID> {
    @Id
    private UUID id;
    @HistoryField
    private String status;
    @HistoryField
    private String message;
    @HistoryField
    private LocalDateTime creationDate;
    @HistoryField
    private LocalDateTime modificationDate;
    @HistoryField
    private LocalDateTime startDate;
    @HistoryField
    private LocalDateTime endDate;
    @HistoryField
    private Boolean notified;
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @HistoryOwnerEntity
    private AsyncTask asyncTask;

    @Override
    public String getTableFilterableColumnValue() {
        return "";
    }

    @Override
    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    @Override
    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID uuid) {
        this.id = uuid;
    }
}
