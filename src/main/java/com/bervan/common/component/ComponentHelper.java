package com.bervan.common.component;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.model.PersistableData;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

public interface ComponentHelper<ID extends Serializable, T extends PersistableData<ID>> {
    AutoConfigurableField buildComponentForField(BervanViewConfig bervanViewConfig, Field field, T item, boolean readOnly) throws IllegalAccessException;

    Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry);
}
