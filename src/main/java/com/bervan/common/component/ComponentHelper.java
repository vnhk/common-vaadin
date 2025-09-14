package com.bervan.common.component;

import com.bervan.common.model.PersistableData;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface ComponentHelper<ID extends Serializable, T extends PersistableData<ID>> {
    List<Field> getVaadinTableFields();

    AutoConfigurableField buildComponentForField(Field field, T item) throws IllegalAccessException;

    Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry);
}
