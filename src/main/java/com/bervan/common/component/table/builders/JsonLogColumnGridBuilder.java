package com.bervan.common.component.table.builders;

import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.JsonLogViewerColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.logging.JsonLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;

import java.io.Serializable;
import java.lang.reflect.Field;

public class JsonLogColumnGridBuilder implements ColumnForGridBuilder {
    private static final JsonLogColumnGridBuilder INSTANCE = new JsonLogColumnGridBuilder();
    private final JsonLogger log = JsonLogger.getLogger(getClass());

    private JsonLogColumnGridBuilder() {

    }

    public static JsonLogColumnGridBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, ClassViewAutoConfigColumn config) {
        return createJsonColumnComponent(field, config);
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(String extension, ClassViewAutoConfigColumn config, Class<T> tClass) {
        return JsonLogViewerColumn.class.getSimpleName().equals(config.getExtension());
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public boolean isSortable() {
        return false;
    }


    protected <ID extends Serializable, T extends PersistableTableData<ID>> ComponentRenderer<Span, T> createJsonColumnComponent(Field f, ClassViewAutoConfigColumn config) {
        return new ComponentRenderer<>(Span::new, jsonColumnUpdater(f));
    }

    private <ID extends Serializable, T extends PersistableTableData<ID>> SerializableBiConsumer<Span, T> jsonColumnUpdater(Field f) {
        return (span, record) -> {
            try {
                span.setClassName("modern-cell-content");
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);

                if (o == null) {
                    span.setText("");
                    return;
                }

                String raw = o.toString().trim();
                JsonNode json = tryParseJson(raw);

                if (json == null) {
                    // Not JSON → normal text
                    span.setText(raw);
                    return;
                }

                String level = getJsonField(json, "level");
                String date = getJsonField(json, "date");
                String msg = getJsonField(json, "msg");

                String result = (level + " " + date + " " + msg).trim();

                span.setText(result);

            } catch (Exception e) {
                log.error("Could not create column in table!", e);
            }
        };
    }

    private JsonNode tryParseJson(String raw) {
        try {
            return new ObjectMapper().readTree(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getJsonField(JsonNode json, String field) {
        JsonNode n = json.get(field);
        return n != null ? n.asText() : "";
    }

}
