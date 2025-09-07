package com.bervan.common.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;


public class BervanImageController extends VerticalLayout implements AutoConfigurableField<List<String>> {
    private final BervanImageViewer viewer;

    public BervanImageController(List<String> imageSource) {
        this.viewer = new BervanImageViewer(imageSource);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        BervanButton deleteCurrent = new BervanButton("Delete current", event -> viewer.removeCurrent());
        BervanButton addNew = new BervanButton("Add new image TBD");

        add(viewer, deleteCurrent, addNew);
    }

    @Override
    public List<String> getValue() {
        return viewer.getValue();
    }

    @Override
    public void setValue(List<String> obj) {
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
}