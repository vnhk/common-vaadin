package com.bervan.common.config;

import com.bervan.logging.JsonLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ViewAutoConfigLoader {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

    private final List<DynamicConfigLoader> loaders;

    private BervanViewConfig configs;

    public ViewAutoConfigLoader(List<DynamicConfigLoader> loaders) {
        this.loaders = loaders;
    }

    @Bean
    public BervanViewConfig viewAutoConfig() throws IOException {
        configs = new BervanViewConfig();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("classpath*:/autoconfig/*.yml");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null) {
                ClassViewAutoConfig config = mapper.readValue(resource.getInputStream(), ClassViewAutoConfig.class);
                String className = filename.replace(".yml", "");
                configs.put(className, new HashMap<>());
                config.getColumns().forEach(column -> configs.get(className).put(column.getField(), column));
            }
        }

        loadDynamicConfigValues();

        return configs;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void loadDynamicConfigValues() {
        if (configs == null) {
            return;
        }

        if (loaders == null || loaders.isEmpty()) {
            return;
        }

        log.info("Loading dynamic values for all BervanViewConfigs");
        for (DynamicConfigLoader loader : loaders) {
            String className = loader.getSupportedClass();
            String fieldName = loader.getSupportedField();
            Map<String, ClassViewAutoConfigColumn> classConfig = configs.get(className);
            ClassViewAutoConfigColumn fieldConfig = classConfig.get(fieldName);
            if (!fieldConfig.isDynamicStrValues()) {
                log.error("Dynamic string values is not supported for {} column!", fieldName);
                throw new RuntimeException("Dynamic string values is not supported for " + fieldName + " column!");
            }

            if (fieldConfig.getDynamicStrValuesMap() != null) {
                log.info("Refreshing dynamic values for {}", fieldName);
            }

            fieldConfig.setDynamicStrValuesMap(loader.getDynamicStrValuesMap());
        }
    }
}
