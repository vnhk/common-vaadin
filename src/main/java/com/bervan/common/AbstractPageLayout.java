package com.bervan.common;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractPageLayout extends VerticalLayout {
    protected final String CURRENT_ROUTE_NAME;

    public AbstractPageLayout(String currentRouteName) {
        CURRENT_ROUTE_NAME = currentRouteName;
    }

    public void notification(String message) {
        Notification.show(message);
    }

}
