package com.bervan.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashMap;

@Configuration
public class ViewAutoConfigLoader {

    @Bean
    public BervanViewConfig viewAutoConfig() throws IOException {
        BervanViewConfig configs = new BervanViewConfig();

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

        return configs;
    }
}
