package com.bervan.common;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractPageView extends VerticalLayout {
    public AbstractPageView() {
        addClassName("bervan-page");
    }
}
