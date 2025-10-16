package com.bervan.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassViewAutoConfig {
    private List<ClassViewAutoConfigColumn> columns;
}
