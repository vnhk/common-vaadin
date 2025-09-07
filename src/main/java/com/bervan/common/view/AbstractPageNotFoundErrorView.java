package com.bervan.common.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractPageNotFoundErrorView extends VerticalLayout {

    public AbstractPageNotFoundErrorView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H1 header = new H1("Page not found!");
        H2 subHeader = new H2("Error 404");
        H3 message = new H3("The page you are looking for does not exist or has been moved.");

        add(header, subHeader, message);
    }
}
