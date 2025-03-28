package com.bervan.common;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BervanImageViewer extends VerticalLayout implements AutoConfigurableField<List<String>> {
    private List<String> imageSources = new ArrayList<>();
    private int currentIndex = 0;
    private final Image imageComponent;
    private final Button prevButton;
    private final Button nextButton;

    public void setImageViewerSize(String maxWidth, String maxHeight) {
        imageComponent.setMaxWidth(maxWidth);
        imageComponent.setMaxHeight(maxHeight);
    }

    public BervanImageViewer(List<String> imageSources) {
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        if (imageSources != null && !imageSources.isEmpty()) {
            this.imageSources.addAll(imageSources);
        }

        imageComponent = new Image();
        imageComponent.setWidth("500px");
        imageComponent.getStyle().setOverflow(Style.Overflow.HIDDEN);

        prevButton = new Button("◀", event -> showPreviousImage());
        nextButton = new Button("▶", event -> showNextImage());

        HorizontalLayout navigation = new HorizontalLayout(prevButton, nextButton);
        navigation.setSpacing(true);

        add(navigation, imageComponent);
        updateView();
    }

    private void updateView() {
        if (imageSources.isEmpty()) {
            imageComponent.setVisible(false);
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            return;
        }

        imageComponent.setVisible(true);
        String source = imageSources.get(currentIndex);

        if (source.startsWith("http") || source.startsWith("https")) {
            imageComponent.setSrc(source);
        } else {
            imageComponent.setSrc("data:image/png;base64," + source);
        }

        prevButton.setEnabled(imageSources.size() > 1);
        nextButton.setEnabled(imageSources.size() > 1);
    }

    private void showPreviousImage() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = imageSources.size() - 1;
        }
        updateView();
    }

    private void showNextImage() {
        if (currentIndex < imageSources.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        updateView();
    }

    public void addImage(String imageSource) {
        imageSources.add(imageSource);
        updateView();
    }

    @Override
    public List<String> getValue() {
        return imageSources;
    }

    @Override
    public void setValue(List<String> imageSources) {
        this.imageSources = imageSources;
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    public void removeCurrent() {
        imageSources.remove(currentIndex);
        if (currentIndex >= imageSources.size()) {
            currentIndex = imageSources.size() - 1;
        }
        updateView();
    }

    public Optional<String> getCurrent() {
        if (!imageSources.isEmpty()) {
            return Optional.ofNullable(imageSources.get(currentIndex));
        }
        return Optional.empty();
    }

    public void updateCurrent(String newImg) {
        imageSources.remove(currentIndex);
        imageSources.add(currentIndex, newImg);
        updateView();
    }
}