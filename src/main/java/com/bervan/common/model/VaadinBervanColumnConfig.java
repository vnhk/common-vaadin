package com.bervan.common.model;

import java.lang.reflect.Field;
import java.util.List;

public class VaadinBervanColumnConfig {
    private Field field;
    private String typeName;
    private String displayName;
    private String internalName;
    private String defaultValue;
    private Class<?> extension;
    private List<String> strValues;
    private List<Integer> intValues;
    private boolean isSingleValue = true;
    private boolean isWysiwyg = false;
    private boolean inTable;
    private boolean sortable;

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

    public boolean isWysiwyg() {
        return isWysiwyg;
    }

    public void setWysiwyg(boolean wysiwyg) {
        isWysiwyg = wysiwyg;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Class<?> getExtension() {
        return extension;
    }

    public void setExtension(Class<?> extension) {
        this.extension = extension;
    }

    public void setInTable(boolean b) {
        this.inTable = b;
    }

    public boolean isInTable() {
        return inTable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isSortable() {
        return sortable;
    }
}
