package com.bervan.common.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.asynctask.AsyncTaskService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
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
import java.util.Objects;

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
        section.addClassName("bervan-filter-section");
        section.setWidthFull();

        H3 sectionTitle = new H3(title);
        sectionTitle.addClassName("bervan-filter-section-title");

        section.add(sectionTitle);
        for (Object component : components) {
            section.add((Component) component);
        }

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
        for (Button button : buttons) {
            button.addClassName("bervan-icon-btn");
        }
        actionButtons.add(buttons);
        actionButtons.setSpacing(true);
        actionButtons.setJustifyContentMode(JustifyContentMode.CENTER);
        return actionButtons;
    }

    public static Div getSearchForm(Component titleComponent, HorizontalLayout actionButtons, HorizontalLayout... formSearchSectionRows) {
        VerticalLayout searchForm = new VerticalLayout();
        searchForm.addClassName("search-form");
        searchForm.setSpacing(true);
        searchForm.setPadding(false);
        searchForm.setWidthFull();
        searchForm.add(formSearchSectionRows);

        // Modern action buttons styling
        actionButtons.addClassName("bervan-filter-actions");
        searchForm.add(actionButtons);

        Div searchContainer = new Div();
        searchContainer.addClassName("search-container");
        searchContainer.addClassName("bervan-filter-panel");

        if (titleComponent != null) {
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        List<AsyncTask> taskNotificationsForUser = AsyncTaskService.getTaskNotificationsForUser();
        for (AsyncTask asyncTask : taskNotificationsForUser) {
            if (Objects.equals(asyncTask.getStatus(), "FAILED")) {
                showErrorNotification("Task failed: " + asyncTask.getMessage(), 10000);
            } else if (Objects.equals(asyncTask.getStatus(), "FINISHED")) {
                if (asyncTask.getMessage() != null && !asyncTask.getMessage().isEmpty()) {
                    showPrimaryNotification("Task finished successfully: " + asyncTask.getMessage(), 10000);
                } else {
                    showPrimaryNotification("Task " + asyncTask.getId().toString() + " finished successfully!", 10000);
                }
            } else {
                continue;
            }

            AsyncTaskService.updateStateToNotified(asyncTask);
        }
    }

    public void showWarningNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }

    public void showSuccessNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showErrorNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void showErrorNotification(String msg, int durationMs) {
        Notification notification = Notification.show(msg, durationMs, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void showPrimaryNotification(String msg, int durationMs) {
        Notification notification = Notification.show(msg, durationMs, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    public void showPrimaryNotification(String msg) {
        Notification notification = Notification.show(msg);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    public void showWarningNotification(String msg, int durationMs) {
        Notification notification = Notification.show(msg, durationMs, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
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
