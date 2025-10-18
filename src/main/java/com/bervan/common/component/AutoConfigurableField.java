package com.bervan.common.component;

public interface AutoConfigurableField<T> {

    T getValue();

    void setValue(T obj);

    void setWidthFull();

    void setId(String id);

    void validate();

    boolean isInvalid();

    void setReadOnly(boolean readOnly);
}
