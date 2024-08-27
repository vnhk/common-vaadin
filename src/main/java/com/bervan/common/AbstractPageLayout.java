package com.bervan.common;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPageLayout extends VerticalLayout {
    protected HorizontalLayout menuButtonsRow = new HorizontalLayout();
    protected final Map<String, Button> buttons = new HashMap<>();
    protected final String CURRENT_ROUTE_NAME;

    public AbstractPageLayout(String currentRouteName) {
        CURRENT_ROUTE_NAME = currentRouteName;
    }

    public void notification(String message) {
        Notification.show(message);
    }

    public void addButton(HorizontalLayout menuRow, String routeName, String buttonText) {
        Button button = new Button(buttonText);
        button.addClickListener(buttonClickEvent ->
                button.getUI().ifPresent(ui -> ui.navigate(routeName)));

        buttons.put(routeName, button);

        if (routeName.equals(CURRENT_ROUTE_NAME)) {
            button.getStyle().set("background-color", "blue");
            button.getStyle().set("color", "white");
        }

        menuRow.add(button);
    }


}
