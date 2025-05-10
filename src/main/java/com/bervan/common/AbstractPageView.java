package com.bervan.common;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import io.micrometer.common.util.StringUtils;

import java.util.List;

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

    protected boolean updateField(String fieldValue, AbstractField field, boolean atLeastOneParameter) {
        if (fieldValue != null && !fieldValue.equals("null")) {
            field.setValue(fieldValue);
            atLeastOneParameter = true;
        }
        return atLeastOneParameter;
    }

    protected boolean updateFieldWithDefault(Object fieldValue, AbstractField field, boolean atLeastOneParameter, Object defaultVal) {
        if (fieldValue != null && !fieldValue.toString().equals("null")) {
            field.setValue(fieldValue);
            atLeastOneParameter = true;
        } else {
            field.setValue(defaultVal);
        }
        return atLeastOneParameter;
    }

    protected Double getDoubleParam(QueryParameters queryParameters, String name) {
        String singleParam = getSingleParam(queryParameters, name);
        if (singleParam == null) {
            return null;
        }
        return Double.valueOf(singleParam);
    }

    protected Integer getIntegerParam(QueryParameters queryParameters, String name) {
        String singleParam = getSingleParam(queryParameters, name);
        if (singleParam == null) {
            return null;
        }
        return Integer.valueOf(singleParam);
    }

    protected static String getString(String shop) {
        if (shop != null && StringUtils.isBlank(shop.trim())) {
            shop = null;
        }
        return shop;
    }


    protected String getSingleParam(QueryParameters queryParameters, String name) {
        List<String> values = queryParameters.getParameters().get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

}
