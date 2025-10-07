package com.bervan.common.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public class AsyncTaskLayout extends MenuNavigationComponent {
    public AsyncTaskLayout(String currentRouteName, boolean details) {
        super(currentRouteName);

        addButtonIfVisible(menuButtonsRow, AbstractAsyncTaskList.ROUTE_NAME, "Home", VaadinIcon.HOME.create());
        if (details) {
            addButtonIfVisible(menuButtonsRow, AbstractAsyncTaskDetails.ROUTE_NAME, "Details", VaadinIcon.INFO_CIRCLE.create());
        }
        add(menuButtonsRow);
    }
}
