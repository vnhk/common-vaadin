package com.bervan.logging;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class BaseProcessContext {
    public static final String PROCESS_NAME = "processName";
    public static final String ROUTE = "route";
    public static final String CTX = "ctx";
    private String processName;
    private String route;
    //    private String userId; if more users

    public Map.Entry<String, Map<String, Object>> map() {
        Map<String, Object> map = new HashMap<>();
        map.put(PROCESS_NAME, processName);
        map.put(ROUTE, route);
        return Map.entry(CTX, map);
    }

}
