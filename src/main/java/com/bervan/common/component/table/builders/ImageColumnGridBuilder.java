package com.bervan.common.component.table.builders;

import com.bervan.common.config.ClassViewAutoConfigColumn;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinImageBervanColumn;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

@Slf4j
public class ImageColumnGridBuilder implements ColumnForGridBuilder {
    private static final ImageColumnGridBuilder INSTANCE = new ImageColumnGridBuilder();

    private ImageColumnGridBuilder() {

    }

    public static ImageColumnGridBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> Renderer<T> build(Field field, ClassViewAutoConfigColumn config) {
        return createImageColumnComponent(field, config);
    }

    @Override
    public <ID extends Serializable, T extends PersistableTableData<ID>> boolean supports(String extension, ClassViewAutoConfigColumn config, Class<T> tClass) {
        return config.getExtension().equals(VaadinImageBervanColumn.class.getSimpleName());
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public boolean isSortable() {
        return false;
    }


    protected <ID extends Serializable, T extends PersistableTableData<ID>> ComponentRenderer<Span, T> createImageColumnComponent(Field f, ClassViewAutoConfigColumn config) {
        return new ComponentRenderer<>(Span::new, imageColumnUpdater(f));
    }

    private <ID extends Serializable, T extends PersistableTableData<ID>> SerializableBiConsumer<Span, T> imageColumnUpdater(Field f) {
        return (span, record) -> {
            try {
                span.setClassName("modern-cell-content");
                span.addClassName("image-cell-content");
                f.setAccessible(true);
                Object o = f.get(record);
                f.setAccessible(false);
                if (o instanceof Collection<?> && !((Collection<?>) o).isEmpty()) {
                    Icon showEditorIcon = new Icon(VaadinIcon.PICTURE);
                    showEditorIcon.addClassName("cell-action-icon");
                    showEditorIcon.addClassName("image-icon");
                    span.add(showEditorIcon);
                }
            } catch (Exception e) {
                log.error("Could not create column in table!", e);
                throw new RuntimeException("Could not create column in table!");
            }
        };
    }

}
