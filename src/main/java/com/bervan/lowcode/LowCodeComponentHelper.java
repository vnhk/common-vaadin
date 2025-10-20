package com.bervan.lowcode;

import com.bervan.common.component.CommonComponentHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LowCodeComponentHelper extends CommonComponentHelper<UUID, LowCodeClass> {

    public LowCodeComponentHelper() {
        super(LowCodeClass.class);
    }

    @Override
    protected List<String> getAllValuesForDynamicDropdowns(String key, LowCodeClass item) {
        if (key.equals("moduleName")) {
            return List.of(
                    "canvas-app",
                    "common-vaadin",
                    "english-text-stats-app",
                    "file-storage-app",
                    "interview-app",
                    "invest-track-app",
                    "learning-language-app",
                    "my-tools",
                    "my-tools-vaadin-app",
                    "pocket-app",
                    "project-mgmt-app",
                    "shopping-stats-server-app",
                    "spreadsheet-app",
                    "streaming-platform-app"
            );
        }
        return new ArrayList<>();
    }
}
