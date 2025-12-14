package com.bervan.common.search.model;

import com.bervan.common.model.BervanOwnedBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class QueryHistory extends BervanOwnedBaseEntity<UUID> {
    @Id
    private UUID id;
    @Size(max = 100)
    private String componentName;
    @Lob
    @Column(length = 5000)
    private String content;

    @Override
    public void setDeleted(Boolean value) {
    }

    @Override
    public Boolean isDeleted() {
        return false;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
