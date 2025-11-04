package com.bervan.common.service;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.view.AbstractFiltersLayout;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record GridActionService<ID extends Serializable, T extends PersistableTableData<ID>>(BaseService<ID, T> service,
                                                                                             AbstractFiltersLayout<ID, T> filtersLayout,
                                                                                             Grid<T> grid,
                                                                                             Function<Void, List<T>> loadData) {
    public void refreshData(List<T> data) {
        data.removeAll(data);
        data.addAll(loadData.apply(null));
    }

    public Set<String> getSelectedItemsByCheckbox(List<Checkbox> checkboxes) {
        return checkboxes.stream()
                .filter(AbstractField::getValue)
                .map(Component::getId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(e -> e.split("checkbox-")[1])
                .collect(Collectors.toSet());
    }

    public void deleteItemsFromGrid(List<T> gridData,
                                    List<T> itemsToDelete,
                                    List<Checkbox> checkboxes) {
        for (T item : itemsToDelete) {
            service.deleteById(item.getId()); //for deleting original
            removeItemFromGrid(item, gridData, checkboxes);
        }

        grid.getDataProvider().refreshAll();
        resetTableResults(filtersLayout, gridData);
    }

    public void resetTableResults(AbstractFiltersLayout<ID, T> filtersLayout, List<T> data) {
        filtersLayout.removeFilters();
        refreshData(data);
    }


    public void removeItemFromGrid(T item, List<T> data, List<Checkbox> checkboxes) {
        int oldSize = data.size();
        data.remove(item);
        if (oldSize == data.size()) {
            ID id = item.getId();
            data.removeIf(e -> e.getId().equals(id));
        }

        ID id = item.getId();
        if (id != null) {
            List<Checkbox> checkboxesToRemove = checkboxes.stream()
                    .filter(AbstractField::getValue)
                    .filter(e -> e.getId().isPresent())
                    .filter(e -> e.getId().get().equals("checkbox-" + id))
                    .toList();
            checkboxes.removeAll(checkboxesToRemove);
        }
    }
}
