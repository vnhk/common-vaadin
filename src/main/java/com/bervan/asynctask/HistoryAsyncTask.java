package com.bervan.asynctask;

import com.bervan.common.model.BervanHistoryEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
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
public class HistoryAsyncTask extends BervanHistoryEntity<UUID> implements PersistableTableData<UUID> {
    @Id
    private UUID id;
    @VaadinBervanColumn(internalName = "status", displayName = "Status", inEditForm = false, inSaveForm = false)
    @HistoryField
    private String status;
    @VaadinBervanColumn(internalName = "message", displayName = "Message", inEditForm = false, inSaveForm = false)
    @HistoryField
    private String message;
    @VaadinBervanColumn(internalName = "creationDate", displayName = "Creation Date", inEditForm = false, inSaveForm = false)
    @HistoryField
    private LocalDateTime creationDate;
    @VaadinBervanColumn(internalName = "modificationDate", displayName = "Modification Date", inEditForm = false, inSaveForm = false)
    @HistoryField
    private LocalDateTime modificationDate;
    @VaadinBervanColumn(internalName = "startDate", displayName = "Start Date", inEditForm = false, inSaveForm = false, inTable = false)
    @HistoryField
    private LocalDateTime startDate;
    @VaadinBervanColumn(internalName = "endDate", displayName = "End Date", inEditForm = false, inSaveForm = false, inTable = false)
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
