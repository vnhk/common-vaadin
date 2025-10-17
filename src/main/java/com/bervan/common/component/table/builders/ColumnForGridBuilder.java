package com.bervan.common.component.table.builders;

import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.vaadin.flow.data.renderer.Renderer;

import java.io.Serializable;
import java.lang.reflect.Field;

public interface ColumnForGridBuilder {
    <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, ClassViewAutoConfigColumn config);

    <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(String extension, ClassViewAutoConfigColumn config, Class<T> tClass);

    boolean isResizable();

    boolean isSortable();

}
