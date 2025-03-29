package com.bervan.common;

public interface AutoConfigurableField<T> {

    T getValue();

    void setValue(T obj);

    void setWidthFull();

    void setId(String id);

    void setReadOnly(boolean readOnly);
}
