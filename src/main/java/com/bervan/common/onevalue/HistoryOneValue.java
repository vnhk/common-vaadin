package com.bervan.common.onevalue;

import com.bervan.common.model.BervanHistoryEntity;
import com.bervan.common.model.PersistableData;
import com.bervan.history.model.HistoryField;
import com.bervan.history.model.HistoryOwnerEntity;
import com.bervan.history.model.HistorySupported;
import com.bervan.ieentities.ExcelIEEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@HistorySupported
public class HistoryOneValue extends BervanHistoryEntity<UUID> implements PersistableData<UUID>, ExcelIEEntity<UUID> {
    @Id
    private UUID id;

    @HistoryField
    private String name;
    @HistoryField
    private String content;
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @HistoryOwnerEntity
    private OneValue oneValue;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    @Override
    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public OneValue getOneValue() {
        return oneValue;
    }

    public void setOneValue(OneValue oneValue) {
        this.oneValue = oneValue;
    }
}
