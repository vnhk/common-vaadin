package com.bervan.common.model;

import java.lang.reflect.Field;
import java.util.List;

public class VaadinTableColumnConfig {
    private Field field;
    private String typeName;
    private String displayName;
    private List<String> strValues;
    private List<Integer> intValues;
    private boolean isSingleValue = true;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getStrValues() {
        return strValues;
    }

    public void setStrValues(List<String> strValues) {
        this.strValues = strValues;
    }

    public List<Integer> getIntValues() {
        return intValues;
    }

    public void setIntValues(List<Integer> intValues) {
        this.intValues = intValues;
    }

    public boolean isSingleValue() {
        return isSingleValue;
    }

    public void setSingleValue(boolean singleValue) {
        isSingleValue = singleValue;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
