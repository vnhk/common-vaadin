package com.bervan.common.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public class AsyncTaskLayout extends MenuNavigationComponent {
    public AsyncTaskLayout(String currentRouteName) {
        super(currentRouteName);
        addButtonIfVisible(menuButtonsRow, AbstractAsyncTaskList.ROUTE_NAME, "Home", VaadinIcon.HOME.create());

    }
}
