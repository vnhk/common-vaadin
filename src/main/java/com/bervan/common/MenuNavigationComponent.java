package com.bervan.common;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class MenuNavigationComponent extends VerticalLayout {
    protected MenuButtonsRow menuButtonsRow = new MenuButtonsRow();
    protected final Map<String, Button> buttons = new HashMap<>();
    protected String CURRENT_ROUTE_NAME;
    protected String[] notVisibleButtonsRoutes;

    public MenuNavigationComponent(String currentRouteName) {
        init(currentRouteName);
    }

    private void init(String currentRouteName) {
        CURRENT_ROUTE_NAME = currentRouteName;
        setWidth("0px");
        setHeight("0px");
        UI.getCurrent().getPage().executeJs("document.querySelectorAll('navigation-buttons').forEach(el => el.remove());");

        UI.getCurrent().getPage().executeJs(
                "document.querySelector('.view-header').appendChild($0);",
                menuButtonsRow.getElement()
        );
    }

    public MenuNavigationComponent(String routeName, String[] notVisibleButtonsRoutes) {
        init(routeName);
        this.notVisibleButtonsRoutes = notVisibleButtonsRoutes;
    }

    public void addButtonIfVisible(HorizontalLayout menuRow, String routeName, String buttonText) {
        boolean inNotVisible = notVisibleButtonsRoutes != null && Arrays.asList(notVisibleButtonsRoutes).contains(routeName);
        if (notVisibleButtonsRoutes == null || !inNotVisible) {
            Button button = new BervanButton(buttonText);
            buttons.put(routeName, button);

            if (routeName.equals(CURRENT_ROUTE_NAME)) {
                button.addClassName("selected-route-button");
            } else {
                button.addClickListener(buttonClickEvent ->
                        button.getUI().ifPresent(ui -> ui.navigateToClient(routeName)));
            }

            menuRow.add(button);
        }
    }

    public void hideButton(String routeName) {
        buttons.get(routeName).setVisible(false);
    }

    public void showButton(String routeName) {
        buttons.get(routeName).setVisible(true);
    }

    public void updateNavigateToForButton(String buttonRouteName, String newRouteValue) {
        buttons.get(buttonRouteName).addClickListener(buttonClickEvent ->
                buttons.get(buttonRouteName).getUI().ifPresent(ui -> ui.navigateToClient(newRouteValue)));
    }

    public void updateButtonText(String buttonRouteName, String newButtonText) {
        buttons.get(buttonRouteName).setText(newButtonText);
    }
}
