package com.bervan.common.controller;

import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.config.EntityConfigValidator;
import com.bervan.common.mapper.BervanDTOMapper;
import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.BaseService;
import com.bervan.core.model.BaseDTO;
import com.bervan.core.model.BaseModel;
import com.bervan.logging.JsonLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseOwnedController<T extends BervanOwnedBaseEntity<ID> & BaseModel<ID>, ID extends Serializable> {

    protected final BaseService<ID, T> service;
    protected final BervanDTOMapper mapper;
    protected final EntityConfigValidator validator;
    private final String entityName;
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

    @Autowired
    private ObjectMapper objectMapper;

    protected BaseOwnedController(BaseService<ID, T> service, BervanDTOMapper mapper, EntityConfigValidator validator, String entityName) {
        this.service = service;
        this.mapper = mapper;
        this.validator = validator;
        this.entityName = entityName;
    }

    private <DTO extends BaseDTO<ID>> List<Field> getModelFieldsUsedInDto(Class<?> modelClass, DTO dto) {
        try {
            Set<String> dtoFields = Arrays.stream(dto.getClass().getDeclaredFields())
                    .filter(field -> !field.getName().equals("id"))
                    .filter(field -> {
                        int mod = field.getModifiers();
                        return !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
                    }).map(e -> e.getName())
                    .collect(Collectors.toSet());

            List<Field> modelFields = Arrays.stream(modelClass.getDeclaredFields())
                    .filter(field -> {
                        int mod = field.getModifiers();
                        return !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
                    })
                    .filter(e -> dtoFields.contains(e.getName()))
                    .collect(Collectors.toList());

            return modelFields;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<DTO> getById(ID id, Class<DTO> dtoClass) {
        return service.loadById(id)
                .map(i -> ResponseEntity.ok(toDto(i, dtoClass)))
                .orElse(ResponseEntity.notFound().build());
    }

    private <DTO extends BaseDTO<ID>> DTO toDto(T i, Class<DTO> dtoClass) {
        return mapper.map(i, dtoClass);
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<?> create(DTO req) {
        return create(req, req.getClass());
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<?> create(DTO req, Class<? extends DTO> resDTOClass) {
        log.debug("Creating entity: {}", req.getClass().getSimpleName());
        T model = (T) mapper.map(req);
        List<EntityConfigValidator.FieldError> errors = validator.validateAll(entityName, model);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
        }

        T saved = service.save(model);
        log.debug("Entity created: {}", saved.getClass().getSimpleName());
        return ResponseEntity.ok(toDto(saved, resDTOClass));
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<?> update(DTO req) {
        log.debug("Updating entity: {}", req.getClass().getSimpleName());
        if (req.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<T> match = service.loadById(req.getId());
        if (match.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        T original = match.get();
        T model = (T) mapper.map(req);

        List<EntityConfigValidator.FieldError> errors = validator.validateAll(entityName, model);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
        }

        List<Field> declaredFields = getModelFieldsUsedInDto(model.getClass(), req);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(model);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            try {
                field.set(original, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        original.setModificationDate(LocalDateTime.now());
        T saved = service.save(original);
        log.debug("Entity updated: {}", saved.getClass().getSimpleName());
        return ResponseEntity.ok(toDto(saved, req.getClass()));
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<?> patchUpdate(DTO req) {
        log.debug("Patch updating entity: {}", req.getClass().getSimpleName());
        if (req.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<T> match = service.loadById(req.getId());
        if (match.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        T original = match.get();
        T model = (T) mapper.map(req);

        List<EntityConfigValidator.FieldError> errors = validator.validateNotNull(entityName, model);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
        }

        List<Field> declaredFields = getModelFieldsUsedInDto(model.getClass(), req);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(model);
                if (value == null) continue;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            try {
                field.set(original, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        original.setModificationDate(LocalDateTime.now());
        T saved = service.save(original);
        log.debug("Entity updated: {}", saved.getClass().getSimpleName());
        return ResponseEntity.ok(toDto(saved, req.getClass()));
    }

    protected ResponseEntity<?> delete(ID id) {
        log.debug("Deleting entity: {}", id);
        Optional<T> match = service.loadById(id);
        if (match.isEmpty()) return ResponseEntity.notFound().build();
        service.delete(match.get());
        log.debug("Entity deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<Page<DTO>> load(int page, int size, Class<DTO> dtoClass) {
        log.debug("Loading entities");
        Set<T> loaded = service.load(PageRequest.of(page, size));
        List<DTO> dtos = loaded.stream()
                .map(p -> map(p, dtoClass))
                .toList();
        int total = Math.toIntExact(service.loadCount());
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        log.debug("Entities loaded: {} - {}", fromIndex, toIndex);
        return ResponseEntity.ok(new PageImpl<>(dtos, PageRequest.of(page, size), total));
    }

    public <DTO extends BaseDTO<ID>> DTO map(T p, Class<DTO> dtoClass) {
        return mapper.map(p, dtoClass);
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<Page<DTO>> load(SearchRequest request, int page, int size, Class<DTO> dtoClass) {
        Set<T> loaded = service.load(request, PageRequest.of(page, size));
        List<DTO> dtos = loaded.stream()
                .map(p -> mapper.map(p, dtoClass))
                .toList();
        int total = Math.toIntExact(service.loadCount(request));
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        return ResponseEntity.ok(new PageImpl<>(dtos, PageRequest.of(page, size), total));
    }

    protected SearchRequest buildSearchRequest(MultiValueMap<String, String> allParams, Class<?> entityClass) {
        SearchRequest request = new SearchRequest();
        if (allParams == null || allParams.isEmpty()) return request;

        Map<String, ClassViewAutoConfigColumn> config = validator.getEntityConfig(entityName);
        if (config.isEmpty()) return request;

        String globalFilter = allParams.getFirst("filter");

        for (Map.Entry<String, ClassViewAutoConfigColumn> entry : config.entrySet()) {
            String fieldName = entry.getKey();
            ClassViewAutoConfigColumn col = entry.getValue();
            if (!col.isFilterable()) continue;

            Field field = findEntityField(entityClass, fieldName);

            boolean hasPredefinedValues =
                    (col.getStrValues() != null && !col.getStrValues().isEmpty()) ||
                            (col.getIntValues() != null && !col.getIntValues().isEmpty()) ||
                            col.isDynamicStrValues();

            if (globalFilter != null && !globalFilter.isBlank() && !hasPredefinedValues
                    && field != null && String.class.equals(field.getType())) {
                request.addCriterion("TEXT_FILTER_GROUP", Operator.OR_OPERATOR, entityClass,
                        fieldName, SearchOperation.LIKE_OPERATION, "%" + globalFilter + "%");
            }

            if (hasPredefinedValues) {
                List<String> values = allParams.get(fieldName);
                if (values != null && !values.isEmpty()) {
                    String groupId = "SELECT_FILTER_" + fieldName.toUpperCase() + "_GROUP";
                    for (String val : values) {
                        if (val != null && !val.isBlank()) {
                            Object typedVal = parseTypedFilterValue(val, field);
                            request.addCriterion(groupId, Operator.OR_OPERATOR, entityClass,
                                    fieldName, SearchOperation.EQUALS_OPERATION, typedVal);
                        }
                    }
                }
                continue;
            }

            if (field == null) continue;
            Class<?> ft = field.getType();

            if (LocalDateTime.class.equals(ft)) {
                applyDateRangeFilter(request, allParams, entityClass, fieldName, true);
            } else if (LocalDate.class.equals(ft)) {
                applyDateRangeFilter(request, allParams, entityClass, fieldName, false);
            } else if (isNumericFieldType(ft)) {
                applyNumericRangeFilter(request, allParams, entityClass, fieldName, ft);
            } else if (Boolean.class.equals(ft) || boolean.class.equals(ft)) {
                String val = allParams.getFirst(fieldName);
                if (val != null && !val.isBlank()) {
                    request.addCriterion("BOOL_FILTER_" + fieldName.toUpperCase() + "_GROUP",
                            entityClass, fieldName, SearchOperation.EQUALS_OPERATION,
                            Boolean.parseBoolean(val));
                }
            } else if (String.class.equals(ft)) {
                String val = allParams.getFirst(fieldName);
                if (val != null && !val.isBlank()) {
                    request.addCriterion("TEXT_FIELD_" + fieldName.toUpperCase() + "_GROUP",
                            entityClass, fieldName, SearchOperation.LIKE_OPERATION,
                            "%" + val + "%");
                }
            }
        }
        return request;
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<Page<DTO>> search(
            MultiValueMap<String, String> allParams, int page, int size,
            Class<DTO> dtoClass, Class<?> entityClass) {
        return load(buildSearchRequest(allParams, entityClass), page, size, dtoClass);
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<Page<DTO>> search(
            SearchRequest baseRequest, MultiValueMap<String, String> allParams,
            int page, int size, Class<DTO> dtoClass, Class<?> entityClass) {
        baseRequest.merge(buildSearchRequest(allParams, entityClass));
        return load(baseRequest, page, size, dtoClass);
    }

    private Field findEntityField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && !c.equals(Object.class)) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private boolean isNumericFieldType(Class<?> t) {
        return Integer.class.equals(t) || int.class.equals(t) ||
                Long.class.equals(t) || long.class.equals(t) ||
                Double.class.equals(t) || double.class.equals(t) ||
                Float.class.equals(t) || float.class.equals(t) ||
                BigDecimal.class.equals(t);
    }

    private void applyDateRangeFilter(SearchRequest req, MultiValueMap<String, String> params,
                                      Class<?> entityClass, String fieldName, boolean isDateTime) {
        String from = params.getFirst(fieldName + "_from");
        String to = params.getFirst(fieldName + "_to");
        String groupId = "RANGE_" + fieldName.toUpperCase() + "_GROUP";
        if (from != null && !from.isBlank()) {
            Object val = parseDateValue(from, isDateTime);
            if (val != null)
                req.addCriterion(groupId, entityClass, fieldName, SearchOperation.GREATER_EQUAL_OPERATION, val);
        }
        if (to != null && !to.isBlank()) {
            Object val = parseDateValue(to, isDateTime);
            if (val != null)
                req.addCriterion(groupId, entityClass, fieldName, SearchOperation.LESS_EQUAL_OPERATION, val);
        }
    }

    private void applyNumericRangeFilter(SearchRequest req, MultiValueMap<String, String> params,
                                         Class<?> entityClass, String fieldName, Class<?> fieldType) {
        String from = params.getFirst(fieldName + "_from");
        String to = params.getFirst(fieldName + "_to");
        String groupId = "RANGE_" + fieldName.toUpperCase() + "_GROUP";
        try {
            if (from != null && !from.isBlank())
                req.addCriterion(groupId, entityClass, fieldName, SearchOperation.GREATER_EQUAL_OPERATION, parseNumericValue(from, fieldType));
            if (to != null && !to.isBlank())
                req.addCriterion(groupId, entityClass, fieldName, SearchOperation.LESS_EQUAL_OPERATION, parseNumericValue(to, fieldType));
        } catch (NumberFormatException ignored) {
        }
    }

    private Object parseDateValue(String value, boolean isDateTime) {
        try {
            if (isDateTime) {
                String v = value.length() == 16 ? value + ":00" : value;
                return LocalDateTime.parse(v);
            } else {
                return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Object parseNumericValue(String value, Class<?> fieldType) {
        if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) return Integer.parseInt(value);
        if (Long.class.equals(fieldType) || long.class.equals(fieldType)) return Long.parseLong(value);
        if (Float.class.equals(fieldType) || float.class.equals(fieldType)) return Float.parseFloat(value);
        if (BigDecimal.class.equals(fieldType)) return new BigDecimal(value);
        return Double.parseDouble(value);
    }

    private Object parseTypedFilterValue(String value, Field field) {
        if (field == null) return value;
        Class<?> t = field.getType();
        try {
            if (Integer.class.equals(t) || int.class.equals(t)) return Integer.parseInt(value);
            if (Long.class.equals(t) || long.class.equals(t)) return Long.parseLong(value);
            if (Boolean.class.equals(t) || boolean.class.equals(t)) return Boolean.parseBoolean(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<byte[]> exportAll(
            MultiValueMap<String, String> allParams, Class<DTO> dtoClass,
            String filenamePrefix, Class<?> entityClass) {
        try {
            SearchRequest request = buildSearchRequest(allParams, entityClass);
            Set<T> all = service.load(request, PageRequest.of(0, 100_000));
            List<DTO> dtos = all.stream().map(e -> mapper.map(e, dtoClass)).toList();
            byte[] data = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dtos);
            String filename = filenamePrefix + "-export-" + LocalDate.now() + ".json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(data);
        } catch (Exception e) {
            log.error("Export failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    protected <DTO extends BaseDTO<ID>> ResponseEntity<ImportResult> importAll(MultipartFile file, Class<DTO> dtoClass) {
        int imported = 0;
        List<String> errors = new ArrayList<>();
        try {
            List<Map<String, Object>> items = objectMapper.readValue(file.getInputStream(), new TypeReference<>() {
            });
            ObjectMapper lenient = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            for (Map<String, Object> item : items) {
                try {
                    item.remove("id");
                    DTO dto = lenient.convertValue(item, dtoClass);
                    T entity = (T) mapper.map(dto);
                    entity.setModificationDate(LocalDateTime.now());
                    service.save(entity);
                    imported++;
                } catch (Exception e) {
                    errors.add(e.getMessage());
                }
            }
            return ResponseEntity.ok(new ImportResult(imported, errors.size(), errors));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ImportResult(0, 0, List.of("Parse error: " + e.getMessage())));
        }
    }
}
