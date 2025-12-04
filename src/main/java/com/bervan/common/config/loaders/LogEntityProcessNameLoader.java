package com.bervan.common.config.loaders;

import com.bervan.common.config.DynamicConfigLoader;
import com.bervan.logging.LogService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogEntityProcessNameLoader implements DynamicConfigLoader {

    private final LogService logService;

    public LogEntityProcessNameLoader(LogService logService) {
        this.logService = logService;
    }

    @Override
    public @NotNull String getSupportedClass() {
        return "LogEntity";
    }

    @Override
    public @NotNull String getSupportedField() {
        return "processName";
    }

    @Override
    public @NotNull List<String> getDynamicStrValuesMap() {
        return logService.loadProcessNames().stream().filter(s -> s != null && !s.isBlank()).toList();
    }
}
