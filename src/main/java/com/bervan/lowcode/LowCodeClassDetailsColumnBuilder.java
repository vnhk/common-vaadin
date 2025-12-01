package com.bervan.lowcode;

import com.bervan.common.component.table.builders.ColumnForGridBuilder;
import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;

import java.io.Serializable;
import java.lang.reflect.Field;

public class LowCodeClassDetailsColumnBuilder implements ColumnForGridBuilder {
    private static final LowCodeClassDetailsColumnBuilder INSTANCE = new LowCodeClassDetailsColumnBuilder();
    private final JsonLogger log = JsonLogger.getLogger(getClass());

    private LowCodeClassDetailsColumnBuilder() {

    }

    public static LowCodeClassDetailsColumnBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, ClassViewAutoConfigColumn config) {
        return createConfigComponent(field, config);
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(String extension, ClassViewAutoConfigColumn config, Class<T> tClass) {
        return VaadinLowCodeClassDetailsColumn.class.getSimpleName().equals(extension);
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    private <ID extends Serializable, T extends PersistableTableData<ID>> ComponentRenderer<Span, T> createConfigComponent(Field f, ClassViewAutoConfigColumn config) {
        return new ComponentRenderer<>(Span::new, configColumnUpdater(f, config));
    }

    private <ID extends Serializable, T extends PersistableTableData<ID>> SerializableBiConsumer<Span, T> configColumnUpdater(Field f, ClassViewAutoConfigColumn config) {
        return (span, record) -> {
            try {
                span.setClassName("modern-cell-content");
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o != null) {
                    span.add(new Div(new Text("Details..."), new Hr()));
                } else {
                    span.add(new Span("Config not created."));
                }
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                throw new RuntimeException("Could not create column in table!");
            }
        };
    }
}
