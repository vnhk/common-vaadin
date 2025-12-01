package com.bervan.common.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class BervanJsonLogController extends VerticalLayout implements AutoConfigurableField<String> {
    private final BervanJsonLogViewer viewer;

    public BervanJsonLogController(String json) {
        this.viewer = new BervanJsonLogViewer(json);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        add(viewer);
    }

    @Override
    public String getValue() {
        return viewer.getValue();
    }

    @Override
    public void setValue(String obj) {
        viewer.setValue(obj);
    }

    @Override
    public void setWidthFull() {
        viewer.setWidthFull();
        super.setWidthFull();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        viewer.setReadOnly(readOnly);
    }

    @Override
    public void validate() {

    }

    @Override
    public boolean isInvalid() {
        return false;
    }
}