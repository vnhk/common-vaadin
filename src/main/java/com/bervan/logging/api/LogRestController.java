package com.bervan.logging.api;

import com.bervan.common.config.EntityConfigValidator;
import com.bervan.common.controller.BaseController;
import com.bervan.common.mapper.BervanDTOMapper;
import com.bervan.logging.LogEntity;
import com.bervan.logging.LogRepository;
import com.bervan.logging.LogService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/logs")
@RolesAllowed("USER")
public class LogRestController extends BaseController<LogEntity, Long> {

    private final LogService logService;
    private final LogRepository logRepository;

    public LogRestController(LogService logService, LogRepository logRepository, BervanDTOMapper mapper, EntityConfigValidator validator) {
        super(logService, mapper, validator, "LogEntity");
        this.logService = logService;
        this.logRepository = logRepository;
    }

    @GetMapping
    public ResponseEntity<Page<LogDto>> listLogs(
            @RequestParam MultiValueMap<String, String> allParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return super.search(allParams, page, size, LogDto.class, LogEntity.class);
    }

    @GetMapping("/trackers")
    public ResponseEntity<Page<LogDto>> listTrackers(
            @RequestParam String appName,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String methodName,
            @RequestParam(required = false) String message,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size,
            @RequestParam(defaultValue = "timestamp") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<LogEntity> result = logRepository.findTrackers(
                appName,
                blankToNull(processName),
                blankToNull(moduleName),
                blankToNull(className),
                blankToNull(methodName),
                blankToNull(message),
                pageable
        );

        Page<LogDto> dtoPage = result.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/app-names")
    public ResponseEntity<Set<String>> getAppNames() {
        return ResponseEntity.ok(logService.loadAppsName());
    }

    @GetMapping("/process-names")
    public ResponseEntity<List<String>> getProcessNames() {
        return ResponseEntity.ok(logService.loadProcessNames());
    }

    @GetMapping("/module-names")
    public ResponseEntity<List<String>> getModuleNames() {
        return ResponseEntity.ok(logService.loadModulesNames());
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportLogs(
            @RequestParam String appName,
            @RequestParam(required = false) String fromTime,
            @RequestParam(required = false) String toTime,
            @RequestParam(required = false) String logLevel,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String moduleName
    ) {
        List<LogEntity> logs = logRepository.findAllForExport(
                appName,
                fromTime != null ? LocalDateTime.parse(fromTime) : null,
                toTime != null ? LocalDateTime.parse(toTime) : null,
                blankToNull(logLevel),
                blankToNull(processName),
                blankToNull(moduleName)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuilder sb = new StringBuilder();
        for (LogEntity log : logs) {
            if (log.getJson() != null && !log.getJson().isEmpty()) {
                sb.append(log.getJson()).append("\n");
            } else {
                sb.append(String.format("[%s] [%s] [%s.%s:%d] %s\n",
                        log.getTimestamp() != null ? log.getTimestamp().format(formatter) : "N/A",
                        log.getLogLevel() != null ? log.getLogLevel() : "N/A",
                        log.getClassName() != null ? log.getClassName() : "N/A",
                        log.getMethodName() != null ? log.getMethodName() : "N/A",
                        log.getLineNumber(),
                        log.getMessage() != null ? log.getMessage() : "N/A"));
            }
        }

        String filename = String.format("logs_%s_%s.txt",
                appName.replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sb.toString());
    }

    private LogDto toDto(LogEntity e) {
        return new LogDto(
                e.getId(),
                e.getApplicationName(),
                e.getLogLevel(),
                e.getClassName(),
                e.getMethodName(),
                e.getProcessName(),
                e.getModuleName(),
                e.getPackageName(),
                e.getRoute(),
                e.getTimestamp(),
                e.getMessage(),
                e.getFullLog(),
                e.getLineNumber()
        );
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
