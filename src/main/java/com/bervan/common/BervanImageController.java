package com.bervan.common;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


public class BervanImageController extends VerticalLayout implements AutoConfigurableField<List<String>> {
    private final BervanImageViewer viewer;

    public BervanImageController(List<String> imageSource) {
        this.viewer = new BervanImageViewer(imageSource);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        BervanButton deleteCurrent = new BervanButton("Delete current", event -> viewer.removeCurrent());
        BervanButton addNew = new BervanButton("Add new image TBD");
        BervanButton storeImageAsBase = new BervanButton("Store image as base64", event -> convertImageToBase64());

        add(viewer, storeImageAsBase, deleteCurrent, addNew);
    }

    private void convertImageToBase64() {
        Optional<String> img = viewer.getCurrent();
        if (img.isPresent() && img.get().startsWith("http")) {
            try (InputStream inputStream = new URL(img.get()).openStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                byte[] imageBytes = outputStream.toByteArray();
                viewer.updateCurrent(Base64.getEncoder().encodeToString(imageBytes));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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