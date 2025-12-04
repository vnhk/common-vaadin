package com.bervan.common.component.table.builders;

import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class LocalDateTimeBuilder implements ColumnForGridBuilder {
    private static final LocalDateTimeBuilder INSTANCE = new LocalDateTimeBuilder();
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

    private LocalDateTimeBuilder() {

    }

    public static LocalDateTimeBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, ClassViewAutoConfigColumn config) {
        return createTextColumnComponent(field, config);
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(String extension, ClassViewAutoConfigColumn config, Class<T> tClass) {
        Field field = Arrays.stream(tClass.getDeclaredFields()).filter(e -> e.getName().equals(config.getField())).findFirst().get();
        return field.getType().getTypeName().equalsIgnoreCase(LocalDateTime.class.getTypeName());
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
                span.addClassName("cell-text");

                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o != null) {
                    LocalDateTime dateTime = (LocalDateTime) o;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yy");
                    span.setText(dateTime.format(formatter));
                }
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                throw new RuntimeException("Could not create column in table!");
            }
        };
    }

}
