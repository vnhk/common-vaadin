package com.bervan.common.onevalue;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public abstract class OneValueFileService {
    private final String startsWithFileName;
    private final String defaultFileName;

    public OneValueFileService(String startsWithFileName, String defaultFileName) {
        this.startsWithFileName = startsWithFileName;
        this.defaultFileName = defaultFileName;
    }

    public void save(OneValue data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = "db/" + startsWithFileName + timestamp + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.write(data.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String load() {
        File directory = new File("db");
        File[] files = directory.listFiles((dir, name) -> name.startsWith(startsWithFileName) && name.endsWith(".txt"));

        if (files != null && files.length > 0) {
            Arrays.sort(files, (f1, f2) -> {
                String timestamp1 = f1.getName().substring(startsWithFileName.length(), f1.getName().length() - 4);
                String timestamp2 = f2.getName().substring(startsWithFileName.length(), f2.getName().length() - 4);
                return timestamp2.compareTo(timestamp1);
            });

            File latestFile = files[0];
            return loadValue(latestFile.getPath());
        } else {
            return loadValue("db/" + defaultFileName);
        }
    }

    private String loadValue(String filePath) {
        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } catch (IOException e) {
            return "";
        }

        return result.toString();
    }

}
