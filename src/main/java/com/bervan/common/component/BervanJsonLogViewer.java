package com.bervan.common.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BervanJsonLogViewer extends VerticalLayout implements AutoConfigurableField<String> {

    private final ObjectMapper mapper;
    private boolean readOnly = false;
    private String value;

    public BervanJsonLogViewer(String json) {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        addClassName("json-log-viewer");
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        setValue(json);
        refresh();
    }

    private String tryFormatJson(String raw) {
        try {
            Object json = mapper.readValue(raw, Object.class);
            String pretty = mapper.writeValueAsString(json);

            // Bold + CSS class for keys
            pretty = pretty.replaceAll(
                    "\"([^\"]+)\"\\s*:",
                    "<span class='json-log-key'>\"$1\"</span>:"
            );

            return pretty;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void refresh() {
        removeAll();
        if (value == null) return;

        String formattedJson = tryFormatJson(value);

        if (formattedJson != null) {
            String processed = formattedJson.replaceAll(
                    "(\"msg\"\\s*:\\s*\"[^\"]*)\\n([^\"\n]*)",
                    "$1<br>$2"
            );

            add(new Html(
                    "<div class='json-log-viewer-pre' " +
                            "style='white-space: pre-wrap; word-break: break-word;'>" +
                            processed +
                            "</div>"
            ));

        } else {
            String html = value.replace("\n", "<br>");
            add(new Html(
                    "<div class='json-log-viewer-text' " +
                            "style='white-space: pre-wrap; word-break: break-word;'>" +
                            html +
                            "</div>"
            ));
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String obj) {
        this.value = obj;
        refresh();
    }

    @Override
    public void validate() {
    }

    @Override
    public boolean isInvalid() {
        return false;
    }
}