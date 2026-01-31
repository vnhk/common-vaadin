package com.bervan.common.component.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Java wrapper for the bervan-floating-toolbar Lit component.
 * Provides a floating action bar for bulk operations on selected items.
 * Supports standard actions (edit, export, delete) and custom actions.
 */
@Tag("bervan-floating-toolbar")
@JsModule("./src/bervan-table/bervan-floating-toolbar.ts")
public class BervanFloatingToolbar extends Component implements HasSize, HasStyle {

    private final List<CustomAction> customActions = new ArrayList<>();
    private final Map<String, Consumer<CustomActionClickEvent>> customActionListeners = new HashMap<>();

    /**
     * Creates a new floating toolbar with default settings.
     */
    public BervanFloatingToolbar() {
        // Listen for custom action clicks
        getElement().addEventListener("custom-action-click", event -> {
            String actionId = event.getEventData().getString("event.detail.actionId");
            Consumer<CustomActionClickEvent> listener = customActionListeners.get(actionId);
            if (listener != null) {
                listener.accept(new CustomActionClickEvent(this, true, actionId));
            }
        }).addEventData("event.detail.actionId");
    }

    /**
     * Sets the number of selected items to display in the badge.
     */
    public void setSelectedCount(int count) {
        getElement().setProperty("selectedCount", count);
    }

    /**
     * Gets the current selected count.
     */
    public int getSelectedCount() {
        return getElement().getProperty("selectedCount", 0);
    }

    /**
     * Enables or disables the edit button.
     */
    public void setEditEnabled(boolean enabled) {
        getElement().setProperty("editEnabled", enabled);
    }

    /**
     * Enables or disables the duplicate button.
     */
    public void setDuplicateEnabled(boolean enabled) {
        getElement().setProperty("duplicateEnabled", enabled);
    }

    /**
     * Enables or disables the export button.
     */
    public void setExportEnabled(boolean enabled) {
        getElement().setProperty("exportEnabled", enabled);
    }

    /**
     * Enables or disables the delete button.
     */
    public void setDeleteEnabled(boolean enabled) {
        getElement().setProperty("deleteEnabled", enabled);
    }

    /**
     * Sets whether edit should only be enabled for single selection.
     */
    public void setSingleSelectOnly(boolean singleOnly) {
        getElement().setProperty("singleSelectOnly", singleOnly);
    }

    /**
     * Adds a custom action button to the toolbar.
     *
     * @param id         Unique identifier for the action
     * @param icon       Vaadin icon name (e.g., "vaadin:check")
     * @param tooltip    Tooltip text shown on hover
     * @param colorClass CSS color class (primary, success, warning, danger, info, accent)
     * @param listener   Click handler for this action
     */
    public void addCustomAction(String id, String icon, String tooltip, String colorClass,
                                Consumer<CustomActionClickEvent> listener) {
        CustomAction action = new CustomAction(id, icon, tooltip, colorClass);
        customActions.add(action);
        customActionListeners.put(id, listener);
        updateCustomActionsProperty();
    }

    /**
     * Removes a custom action by its ID.
     */
    public void removeCustomAction(String id) {
        customActions.removeIf(a -> a.id.equals(id));
        customActionListeners.remove(id);
        updateCustomActionsProperty();
    }

    /**
     * Clears all custom actions.
     */
    public void clearCustomActions() {
        customActions.clear();
        customActionListeners.clear();
        updateCustomActionsProperty();
    }

    private void updateCustomActionsProperty() {
        JsonArray jsonArray = Json.createArray();
        for (int i = 0; i < customActions.size(); i++) {
            CustomAction action = customActions.get(i);
            JsonObject obj = Json.createObject();
            obj.put("id", action.id);
            obj.put("icon", action.icon);
            obj.put("tooltip", action.tooltip);
            obj.put("colorClass", action.colorClass);
            jsonArray.set(i, obj);
        }
        getElement().setPropertyJson("customActions", jsonArray);
    }

    /**
     * Adds a listener for edit button clicks.
     */
    public void addEditClickListener(ComponentEventListener<EditClickEvent> listener) {
        addListener(EditClickEvent.class, listener);
    }

    /**
     * Adds a listener for duplicate button clicks.
     */
    public void addDuplicateClickListener(ComponentEventListener<DuplicateClickEvent> listener) {
        addListener(DuplicateClickEvent.class, listener);
    }

    /**
     * Adds a listener for export button clicks.
     */
    public void addExportClickListener(ComponentEventListener<ExportClickEvent> listener) {
        addListener(ExportClickEvent.class, listener);
    }

    /**
     * Adds a listener for delete button clicks.
     */
    public void addDeleteClickListener(ComponentEventListener<DeleteClickEvent> listener) {
        addListener(DeleteClickEvent.class, listener);
    }

    /**
     * Adds a listener for close button clicks.
     */
    public void addCloseClickListener(ComponentEventListener<CloseClickEvent> listener) {
        addListener(CloseClickEvent.class, listener);
    }

    @DomEvent("edit-click")
    public static class EditClickEvent extends ComponentEvent<BervanFloatingToolbar> {
        public EditClickEvent(BervanFloatingToolbar source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("duplicate-click")
    public static class DuplicateClickEvent extends ComponentEvent<BervanFloatingToolbar> {
        public DuplicateClickEvent(BervanFloatingToolbar source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("export-click")
    public static class ExportClickEvent extends ComponentEvent<BervanFloatingToolbar> {
        public ExportClickEvent(BervanFloatingToolbar source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("delete-click")
    public static class DeleteClickEvent extends ComponentEvent<BervanFloatingToolbar> {
        public DeleteClickEvent(BervanFloatingToolbar source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("close-click")
    public static class CloseClickEvent extends ComponentEvent<BervanFloatingToolbar> {
        public CloseClickEvent(BervanFloatingToolbar source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event fired when a custom action button is clicked.
     */
    public static class CustomActionClickEvent extends ComponentEvent<BervanFloatingToolbar> {
        private final String actionId;

        public CustomActionClickEvent(BervanFloatingToolbar source, boolean fromClient, String actionId) {
            super(source, fromClient);
            this.actionId = actionId;
        }

        public String getActionId() {
            return actionId;
        }
    }

    /**
     * Internal class to hold custom action data.
     */
    private static class CustomAction {
        final String id;
        final String icon;
        final String tooltip;
        final String colorClass;

        CustomAction(String id, String icon, String tooltip, String colorClass) {
            this.id = id;
            this.icon = icon;
            this.tooltip = tooltip;
            this.colorClass = colorClass;
        }
    }
}
