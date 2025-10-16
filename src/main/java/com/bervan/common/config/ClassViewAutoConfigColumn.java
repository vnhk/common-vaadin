package com.bervan.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassViewAutoConfigColumn {
    private String field;
    private String internalNameField;
    private String internalName;
    private String displayName;
    private List<String> strValues;
    private List<Integer> intValues;
    private boolean inSaveForm;
    private boolean inEditForm;
    private boolean inTable;
    private String extension;
    private boolean required;
}
