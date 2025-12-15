package com.bervan.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassViewAutoConfigColumn {
    private List<String> dynamicStrValuesList;
    private String field;
    private String internalNameField;
    private String internalName;
    private String displayName;
    private List<String> strValues;
    private List<Integer> intValues;
    private boolean inSaveForm;
    private boolean wysiwyg;
    private boolean inEditForm;
    private boolean inTable;
    private boolean fetchable = true;
    private boolean filterable = true;
    private String extension;
    private Object defaultValue;
    private boolean required = false;
    private boolean dynamicStrValues = false;
    private boolean sortable; // needs to be updated in yaml file!
    private Integer min;
    private Integer max;

    public Integer getMin() {
        if (min == null) {
            return 0;
        }
        return min;
    }

    public Integer getMax() {
        if (max == null) {
            return Integer.MAX_VALUE;
        }
        return max;
    }
}
