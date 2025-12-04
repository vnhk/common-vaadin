package com.bervan.common.component.table.builders;

import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;

import java.io.Serializable;
import java.lang.reflect.Field;

public class TextColumnGridBuilder implements ColumnForGridBuilder {
    private static final TextColumnGridBuilder INSTANCE = new TextColumnGridBuilder();
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

    private TextColumnGridBuilder() {

    }

    public static TextColumnGridBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, ClassViewAutoConfigColumn config) {
        return createTextColumnComponent(field, config);
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(String extension, ClassViewAutoConfigColumn config, Class<T> tClass) {
        return true; //default
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public boolean isSortable() {
        return true;
    }


    protected <ID extends Serializable, T extends PersistableTableData<ID>> ComponentRenderer<Span, T> createTextColumnComponent(Field f, ClassViewAutoConfigColumn config) {
        return new ComponentRenderer<>(Span::new, textColumnUpdater(f, config));
    }

    private <ID extends Serializable, T extends PersistableTableData<ID>> SerializableBiConsumer<Span, T> textColumnUpdater(Field f, ClassViewAutoConfigColumn config) {
        return (span, record) -> {
            try {
                span.setClassName("modern-cell-content");
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o != null) {
                    if (config.isWysiwyg()) {
                        Icon showEditorIcon = new Icon(VaadinIcon.EDIT);
                        showEditorIcon.addClassName("cell-action-icon");
                        span.add(showEditorIcon);
                    } else {
                        Span textContent = new Span(o.toString());
                        textContent.addClassName("cell-text");
                        span.add(textContent);
                    }
                }
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                throw new RuntimeException("Could not create column in table!");
            }
        };
    }

}
