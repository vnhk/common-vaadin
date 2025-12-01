package com.bervan.common;

import com.bervan.common.component.BervanButton;
import com.bervan.common.view.MenuButtonsRow;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class MenuNavigationComponent extends VerticalLayout {
    protected final Map<String, Button> buttons = new HashMap<>();
    protected MenuButtonsRow menuButtonsRow = new MenuButtonsRow();
    @Getter
    protected String currentRouteName;
    protected String[] notVisibleButtonsRoutes;

    public MenuNavigationComponent(String currentRouteName) {
        init(currentRouteName);
    }

    public MenuNavigationComponent(String routeName, String[] notVisibleButtonsRoutes) {
        init(routeName);
        this.notVisibleButtonsRoutes = notVisibleButtonsRoutes;
    }

    private void init(String currentRouteName) {
        menuButtonsRow.addClassName("menu-container");
        this.currentRouteName = currentRouteName;
        setWidth("0px");
        setHeight("0px");
        UI.getCurrent().getPage().executeJs("document.querySelectorAll('navigation-buttons').forEach(el => el.remove());");

        UI.getCurrent().getPage().executeJs(
                "document.querySelector('.view-header').appendChild($0);",
                menuButtonsRow.getElement()
        );
    }

    public void addButtonIfVisible(HorizontalLayout menuRow, String routeName, String buttonText, Icon icon) {
        boolean inNotVisible = notVisibleButtonsRoutes != null
                && Arrays.asList(notVisibleButtonsRoutes).contains(routeName);

        if (notVisibleButtonsRoutes == null || !inNotVisible) {
            Button button = new BervanButton(buttonText, icon);
            button.addClassName("navigation-button");
            buttons.put(routeName, button);

            if (routeName.equals(currentRouteName)) {
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
