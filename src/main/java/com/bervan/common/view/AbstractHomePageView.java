package com.bervan.common.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

public abstract class AbstractHomePageView extends VerticalLayout {

    public AbstractHomePageView() {
        addClassName("home-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    protected Component createHeader(String welcomeHeaderText, String motivationText) {
        H1 welcomeHeader = new H1(welcomeHeaderText);
        welcomeHeader.addClassName("welcome-text");

        Paragraph motivation = new Paragraph(motivationText);
        motivation.addClassName("motivation-text");

        Div headerLayout = new Div(welcomeHeader, motivation);
        headerLayout.addClassName("header-section");
        return headerLayout;
    }

    protected Component createQuickAccessSection(List<String> title, List<String> description, List<Icon> icons, List<String> routes) {
        if (title.size() != description.size() || title.size() != icons.size() || title.size() != routes.size()) {
            throw new IllegalArgumentException("All lists must have the same size.");
        }

        FlexLayout quickAccessLayout = new FlexLayout();
        quickAccessLayout.addClassName("quick-access-grid");
        quickAccessLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        quickAccessLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        for (int i = 0; i < title.size(); i++) {
            quickAccessLayout.add(createCardButton(
                    icons.get(i),
                    title.get(i),
                    description.get(i),
                    routes.get(i)
            ));
        }

        return quickAccessLayout;
    }

    // Helper method for creating clickable card buttons
    private Div createCardButton(Icon icon, String title, String description, String route) {
        Div cardButtonDiv = new Div();
        cardButtonDiv.addClassName("card-button");

        // Set icon and text inside the button
        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);

        // Icon styling (e.g., cyan/pink to match the menu theme)
        icon.addClassName("card-icon");

        H4 titleText = new H4(title);
        titleText.addClassName("card-title");

        Paragraph descText = new Paragraph(description);
        descText.addClassName("card-description");

        content.add(icon, titleText, descText);
        cardButtonDiv.add(content);

        // Button layout settings
        cardButtonDiv.setWidth("180px");
        cardButtonDiv.setHeight("180px");
        cardButtonDiv.getStyle().set("padding", "10px");

        cardButtonDiv.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigateToClient(route));
        });

        return cardButtonDiv;
    }


    protected Div createFooterSection(String titleText) {
        Div statsPanel = new Div();
        statsPanel.addClassName("stats-panel");

        H3 title = new H3(titleText);
        title.addClassName("stats-title");
        statsPanel.add(title);

        return statsPanel;
    }

}
