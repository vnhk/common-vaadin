package com.bervan.lowcode;

import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.model.PersistableTableOwnedData;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LowCodeClassDetails extends BervanOwnedBaseEntity<UUID> implements PersistableTableOwnedData<UUID> {
    @Id
    private UUID id;
    private String field;
    private String displayName;
    private String type;
    private String defaultValue;
    private Boolean inSaveForm;
    private Boolean inEditForm;
    private Boolean inTable;
    private Boolean required;
    private Integer min;
    private Integer max;
    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "low_code_class_id", nullable = false)
    private LowCodeClass lowCodeClass;

    @Override
    public String getTableFilterableColumnValue() {
        return field;
    }

    @Override
    public void setDeleted(Boolean value) {
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
