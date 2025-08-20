package com.bervan.common;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractPageNotFoundError extends VerticalLayout {

    public AbstractPageNotFoundError() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H1 header = new H1("Page not found!");
        H2 subHeader = new H2("Error 404");
        Paragraph message = new Paragraph("The page you are looking for does not exist or has been moved.");

        add(header, subHeader, message);
    }
}
