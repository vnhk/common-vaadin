package com.bervan.common;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractPageView extends VerticalLayout {
    public AbstractPageView() {
        addClassName("bervan-page");
    }

    public void showErrorNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void showWarningNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    public void showSuccessNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showPrimaryNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    protected HorizontalLayout getDialogTopBarLayout(Dialog dialog) {
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addClassName("option-button");

        closeButton.addClickListener(e -> dialog.close());
        HorizontalLayout headerLayout = new HorizontalLayout(closeButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return headerLayout;
    }
}
