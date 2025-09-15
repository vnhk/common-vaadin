package com.bervan.common.component.table.builders;

import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinBervanColumnConfig;
import com.vaadin.flow.data.renderer.Renderer;

import java.io.Serializable;
import java.lang.reflect.Field;

public interface ColumnForGridBuilder {
    <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, VaadinBervanColumnConfig config);

    <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(Class<?> extension, VaadinBervanColumnConfig config, Class<T> tClass);

    boolean isResizable();

    boolean isSortable();

}
