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
        addClassName("route-buttons-layout");
        CURRENT_ROUTE_NAME = currentRouteName;
    }

    public void addButton(HorizontalLayout menuRow, String routeName, String buttonText) {
        Button button = new Button(buttonText);
        button.addClickListener(buttonClickEvent ->
                button.getUI().ifPresent(ui -> ui.navigate(routeName)));

        buttons.put(routeName, button);

        button.addClassName("option-button");

        if (routeName.equals(CURRENT_ROUTE_NAME)) {
            button.addClassName("selected-route-button");
        }

        menuRow.add(button);
    }


}
