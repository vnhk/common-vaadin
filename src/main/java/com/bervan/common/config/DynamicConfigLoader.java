package com.bervan.common.config;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface DynamicConfigLoader {
    @NotNull String getSupportedClass();

    @NotNull String getSupportedField();

    @NotNull List<String> getDynamicStrValuesMap();
}
