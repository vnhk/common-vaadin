package com.bervan.common.view;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
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

    protected static String getString(String value) {
        if (value != null && StringUtils.isBlank(value.trim())) {
            value = null;
        }
        return value;
    }

    public static Div createSearchSection(String title, Object... components) {
        Div section = new Div();
        section.addClassName("search-section");
        section.getStyle()
                .set("margin-bottom", "0")
                .set("margin-left", "10px")
                .set("margin-right", "10px")
                .set("padding", "0.5rem")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("height", "fit-content")
                .set("min-height", "120px")
                .set("min-width", "0"); // Allows flex items to shrink

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        section.add(sectionTitle);
        for (Object component : components) {
            section.add((Component) component);
        }

        section.getStyle().set("flex", "1");

        return section;
    }

    public static HorizontalLayout createSearchSectionRow(Div... divs) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.add(divs);
        return row;
    }

    public static HorizontalLayout getSearchActionButtonsLayout(Button... buttons) {
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.add(buttons);
        actionButtons.setSpacing(true);
        actionButtons.setJustifyContentMode(JustifyContentMode.CENTER);
        actionButtons.getStyle()
                .set("margin-top", "1rem")
                .set("margin-left", "10px")
                .set("margin-bottom", "1rem");
        return actionButtons;
    }

    public static Div getSearchForm(Component titleComponent, HorizontalLayout actionButtons, HorizontalLayout... formSearchSectionRows) {
        VerticalLayout searchForm = new VerticalLayout();
        searchForm.addClassName("search-form");
        searchForm.setSpacing(true);
        searchForm.setPadding(false);
        searchForm.setWidthFull();
        searchForm.add(formSearchSectionRows);
        searchForm.add(actionButtons);

        Div searchContainer = new Div();
        searchContainer.addClassName("search-container");
        searchContainer.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "2rem")
                .set("width", "100%");

        if (titleComponent != null) {
            titleComponent.getStyle().set("margin-left", "10px")
                    .set("margin-top", "1rem")
                    .set("margin-bottom", "1rem");
            searchContainer.add(titleComponent, searchForm);
        } else {
            searchContainer.add(searchForm);
        }

        return searchContainer;
    }

    public static HorizontalLayout createSearchFieldRow(List<? extends Component> fields) {
        return createSearchFieldRow(fields.toArray(new Component[0]));
    }

    public static HorizontalLayout createSearchFieldRow(Component... fields) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        for (Component component : fields) {
            component.getElement().getStyle().set("flex", "1");
            row.add(component);
        }

        return row;
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
        String singleParam = (String) getParams(queryParameters, name);
        if (singleParam == null) {
            return null;
        }
        return Double.valueOf(singleParam);
    }

    protected Integer getIntegerParam(QueryParameters queryParameters, String name) {
        String singleParam = (String) getParams(queryParameters, name);
        if (singleParam == null) {
            return null;
        }
        return Integer.valueOf(singleParam);
    }

    protected Object getParams(QueryParameters queryParameters, String name) {
        List<String> values = queryParameters.getParameters().get(name);
        if (values == null) {
            return null;
        }
        if (values.size() == 1) {
            return values.get(0);
        } else if (values.isEmpty()) {
            return null;
        }
        return values;
    }

}
