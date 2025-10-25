package com.bervan.lowcode.generator;

import com.bervan.lowcode.LowCodeClass;
import com.bervan.lowcode.LowCodeClassDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
@Profile("local")
public class LocalLowCodeGenerator implements LowCodeGenerator {
    @Value("${low-code.project-root-path}")
    private String projectRootPath;

    @Override
    public void generate(LowCodeClass obj) throws IOException {
        if (projectRootPath.isBlank()) {
            throw new RuntimeException("Project root path is not set");
        }

        // Build the directory path: moduleName + packageName
        String path = obj.getModuleName() + File.separator
                + "src" + File.separator
                + "main" + File.separator
                + "java" + File.separator
                + obj.getPackageName().replace('.', '/');
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs(); // create all directories if they don't exist
        }

        // Build the content of the files
        createMainEntityClass(obj, path);
        createYmlAutoConfig(obj);
        createMainRepositoryClass(obj, path);
        createMainServiceClass(obj, path);
//        createAbstractView(obj, path);
//        if (obj.getRouteName() != null && !obj.getRouteName().isEmpty()) {
//            createView(obj);
//        }
    }

    private void createYmlAutoConfig(LowCodeClass obj) {
        String path = obj.getModuleName() + File.separator
                + "src" + File.separator
                + "main" + File.separator
                + "resources" + File.separator
                + "autoconfig";

        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String ymlFilePath = path + File.separator + obj.getClassName() + ".yml";
        File ymlFile = new File(ymlFilePath);
        try {
            ymlFile.createNewFile();

            StringBuilder content = new StringBuilder();
            content.append("columns:\n");
            for (LowCodeClassDetails lowCodeClassDetail : obj.getLowCodeClassDetails()) {
                content.append("  - field: ").append(lowCodeClassDetail.getField()).append("\n");
                content.append("    displayName: \"").append(lowCodeClassDetail.getDisplayName()).append("\"\n");
                content.append("    internalName: ").append(lowCodeClassDetail.getField()).append("\n");
                content.append("    inSaveForm: ").append(lowCodeClassDetail.getInSaveForm()).append("\n");
                content.append("    inEditForm: ").append(lowCodeClassDetail.getInEditForm()).append("\n");
                content.append("    inTable: ").append(lowCodeClassDetail.getInTable()).append("\n");
                content.append("    required: ").append(lowCodeClassDetail.getRequired()).append("\n");
                if (lowCodeClassDetail.getMin() != null) {
                    content.append("    min: ").append(lowCodeClassDetail.getMin()).append("\n");
                }
                if (lowCodeClassDetail.getMax() != null) {
                    content.append("    max: ").append(lowCodeClassDetail.getMax()).append("\n");
                }
                content.append("\n");
            }


            try (FileWriter writer = new FileWriter(ymlFile)) {
                writer.write(content.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create YAML configuration file", e);
        }
    }

    private void createMainRepositoryClass(LowCodeClass obj, String path) throws IOException {
        // Define the path to the Java file
        String javaFilePath = path + File.separator + obj.getClassName() + "Repository.java";
        File javaFile = new File(javaFilePath);
        if (!javaFile.exists()) {
            javaFile.createNewFile(); // create the Java file if it doesn't exist
        }

        StringBuilder content = new StringBuilder();
        content.append("package ").append(obj.getPackageName()).append(";\n\n");
        content.append("""
                import com.bervan.history.model.BaseRepository;
                import org.springframework.stereotype.Repository;
                
                import java.util.UUID;
                """);
        content.append("\n");
        content.append("// Low-Code START\n");
        content.append("""
                @Repository
                """);
        content.append("public interface ").append(obj.getClassName()).append("Repository")
                .append(" extends BaseRepository<").append(obj.getClassName()).append(", UUID> {\n\n");
        content.append("}\n");
        content.append("// Low-Code END\n");

        // Write content to the Java file
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(content.toString());
        }
    }

    private void createMainServiceClass(LowCodeClass obj, String path) throws IOException {
        // Define the path to the Java file
        String javaFilePath = path + File.separator + obj.getClassName() + "Service.java";
        File javaFile = new File(javaFilePath);
        if (!javaFile.exists()) {
            javaFile.createNewFile(); // create the Java file if it doesn't exist
        }

        StringBuilder content = new StringBuilder();
        content.append("package ").append(obj.getPackageName()).append(";\n\n");
        content.append("""
                import com.bervan.common.search.SearchService;
                import com.bervan.common.service.BaseService;
                import com.bervan.history.model.BaseRepository;
                import org.springframework.stereotype.Service;
                
                import java.util.List;
                import java.util.UUID;
                """);
        content.append("\n");
        content.append("// Low-Code START\n");
        content.append("""
                @Service
                """);
        content.append("public class ").append(obj.getClassName()).append("Service")
                .append(" extends BaseService<UUID, ").append(obj.getClassName()).append("> {\n\n");
        content.append("    public ").append(obj.getClassName()).append("Service(BaseRepository<").append(obj.getClassName()).append(", UUID> repository, SearchService searchService) {\n");
        content.append("        super(repository, searchService);\n");
        content.append("    }\n\n");
        content.append("}\n");
        content.append("// Low-Code END\n");

        // Write content to the Java file
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(content.toString());
        }
    }

    private void createMainEntityClass(LowCodeClass obj, String path) throws IOException {
        // Define the path to the Java file
        String javaFilePath = path + File.separator + obj.getClassName() + ".java";
        File javaFile = new File(javaFilePath);
        if (!javaFile.exists()) {
            javaFile.createNewFile(); // create the Java file if it doesn't exist
        }

        StringBuilder content = new StringBuilder();
        content.append("package ").append(obj.getPackageName()).append(";\n\n");
        content.append("""
                import com.bervan.common.model.BervanBaseEntity;
                import com.bervan.common.model.PersistableTableData;
                import java.util.*;
                import java.time.LocalDateTime;
                import jakarta.persistence.*;
                import lombok.Getter;
                import lombok.Setter;
                """);
        content.append("\n");
        content.append("// Low-Code START\n");
        content.append("""
                @Entity
                @Getter
                @Setter
                @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
                """);
        content.append("public class ").append(obj.getClassName())
                .append(" extends BervanBaseEntity<UUID> implements PersistableTableData<UUID> {\n\n");
        content.append("    // Default constructor\n");
        content.append("    public ").append(obj.getClassName()).append("() {\n");
        content.append("        // constructor body\n");
        content.append("    }\n\n");
        content.append("    @Id\n");
        content.append("    private UUID id;\n\n");
        content.append("    private Boolean deleted = false;\n\n");
        content.append("    private LocalDateTime modificationDate;\n\n");

        for (LowCodeClassDetails lowCodeClassDetail : obj.getLowCodeClassDetails()) {
            content.append("    private ").append(lowCodeClassDetail.getType()).append(" ").append(lowCodeClassDetail.getField()).append(";\n\n");
        }

        content.append("""
                    @Override
                    public Boolean isDeleted() {
                        return deleted;
                    }
                
                    @Override
                    public void setDeleted(Boolean value) {
                        this.deleted = value;
                    }
                
                    @Override
                    public LocalDateTime getModificationDate() {
                        return modificationDate;
                    }
                
                    @Override
                    public void setModificationDate(LocalDateTime modificationDate) {
                        this.modificationDate = modificationDate;
                    }
                
                    @Override
                    public String getTableFilterableColumnValue() {
                        return id.toString();
                    }
                """);

        content.append("}\n");
        content.append("// Low-Code END\n");

        // Write content to the Java file
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(content.toString());
        }
    }
}
