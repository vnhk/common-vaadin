package com.bervan.common.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBasedConfigUtils {
    public static List<File> loadFilesInStorageFolder(String PATH_TO_STORAGE, String pathInFileStorage) {
        List<File> fileInfos = new ArrayList<>();
        try {
            scanDirectory(new File(PATH_TO_STORAGE + pathInFileStorage), fileInfos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileInfos;
    }

    public static void scanDirectory(File fileParent, List<File> fileInfos) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(fileParent.getAbsolutePath()))) {
            Set<Path> collect = paths.filter(path -> !path.toAbsolutePath()
                            .equals(fileParent.toPath().toAbsolutePath()))
                    .collect(Collectors.toSet());
            for (Path path : collect) {
                File file = path.toFile();
                if (file.isDirectory()) {
                    fileInfos.add(file);
                    scanDirectory(file, fileInfos);
                } else {
                    fileInfos.add(file);
                }
            }
        }
    }
}
