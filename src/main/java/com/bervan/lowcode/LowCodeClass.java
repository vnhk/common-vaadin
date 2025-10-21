package com.bervan.lowcode;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LowCodeClass extends BervanBaseEntity<UUID> implements PersistableTableData<UUID> {
    @Id
    private UUID id;
    private String moduleName;
    private String packageName;
    private String routeName;
    private String status;
    private boolean historyEnabled = false;
    private String className;
    private Boolean deleted = false;
    @OneToMany(mappedBy = "lowCodeClass", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LowCodeClassDetails> lowCodeClassDetails = new ArrayList<>();

    @Override
    public String getTableFilterableColumnValue() {
        return className;
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
