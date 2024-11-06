package com.bervan.common;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashMap;
import java.util.Map;

public abstract class MenuNavigationComponent extends VerticalLayout {
    protected MenuButtonsRow menuButtonsRow = new MenuButtonsRow();
    protected final Map<String, Button> buttons = new HashMap<>();
    protected final String CURRENT_ROUTE_NAME;

    public MenuNavigationComponent(String currentRouteName) {
        CURRENT_ROUTE_NAME = currentRouteName;
        setWidth("0px");
        setHeight("0px");
        UI.getCurrent().getPage().executeJs("document.querySelectorAll('navigation-buttons').forEach(el => el.remove());");

        UI.getCurrent().getPage().executeJs(
                "document.querySelector('.view-header').appendChild($0);",
                menuButtonsRow.getElement()
        );
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
