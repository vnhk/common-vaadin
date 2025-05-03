package com.bervan.common.user;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserToUserRelation extends BervanBaseEntity<UUID> implements PersistableTableData<UUID> {
    @Id
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;
    @ManyToOne
    @JoinColumn(name = "child_id")
    private User child;
    private String description;
    private LocalDateTime modificationDate;

    @Override
    public String getTableFilterableColumnValue() {
        return parent.getUsername() + " - " + child.getUsername() + ": " + description;
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

    public void setChild(User child) {
        this.child = child;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public User getParent() {
        return parent;
    }

    public User getChild() {
        return child;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void setDeleted(Boolean value) {

    }
}
