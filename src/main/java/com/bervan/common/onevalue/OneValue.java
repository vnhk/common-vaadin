package com.bervan.common.onevalue;

import com.bervan.common.model.BaseOneValue;
import com.bervan.common.model.PersistableData;
import com.bervan.common.user.User;
import com.bervan.history.model.AbstractBaseEntity;
import com.bervan.history.model.HistoryCollection;
import com.bervan.history.model.HistorySupported;
import com.bervan.ieentities.ExcelIEEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@HistorySupported
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "owner.id"})
)
public class OneValue implements BaseOneValue, PersistableData<UUID>, AbstractBaseEntity<UUID>, ExcelIEEntity<UUID> {

    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    @Lob
    private String content;
    private LocalDateTime modificationDate;

    @ManyToOne
    private User owner;

    @OneToMany(fetch = FetchType.EAGER)
    @HistoryCollection(historyClass = HistoryOneValue.class)
    private Set<HistoryOneValue> history = new HashSet<>();

    public static OneValue of(String name, String content) {
        OneValue oneValue = new OneValue();
        oneValue.name = name;
        oneValue.content = content;
        return oneValue;
    }

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

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User user) {
        this.owner = user;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<HistoryOneValue> getHistory() {
        return history;
    }

    public void setHistory(Set<HistoryOneValue> history) {
        this.history = history;
    }
}
