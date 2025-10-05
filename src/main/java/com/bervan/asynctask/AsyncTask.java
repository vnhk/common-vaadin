package com.bervan.asynctask;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumn;
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
    @VaadinBervanColumn(internalName = "status", displayName = "Status", inEditForm = false, inSaveForm = false)
    private String status;
    @VaadinBervanColumn(internalName = "message", displayName = "Message", inEditForm = false, inSaveForm = false)
    private String message;
    @VaadinBervanColumn(internalName = "creationDate", displayName = "Creation Date", inEditForm = false, inSaveForm = false)
    private LocalDateTime creationDate;
    @VaadinBervanColumn(internalName = "modificationDate", displayName = "Modification Date", inEditForm = false, inSaveForm = false)
    private LocalDateTime modificationDate;
    @VaadinBervanColumn(internalName = "startDate", displayName = "Start Date", inEditForm = false, inSaveForm = false)
    private LocalDateTime startDate;
    @VaadinBervanColumn(internalName = "endDate", displayName = "End Date", inEditForm = false, inSaveForm = false)
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
