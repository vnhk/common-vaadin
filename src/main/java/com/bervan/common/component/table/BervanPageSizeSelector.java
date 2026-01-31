package com.bervan.common.component.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Java wrapper for the bervan-page-size-selector Lit component.
 * Provides a dropdown for selecting page size with localStorage persistence.
 */
@Tag("bervan-page-size-selector")
@JsModule("./src/bervan-table/bervan-page-size-selector.ts")
public class BervanPageSizeSelector extends Component implements HasSize, HasStyle {

    /**
     * Creates a new page size selector with default options.
     */
    public BervanPageSizeSelector() {
        setOptions(List.of(10, 25, 50, 100, -1));
        setValue(50);
    }

    /**
     * Creates a new page size selector with specified options.
     *
     * @param options The available page size options (-1 for "All")
     */
    public BervanPageSizeSelector(List<Integer> options) {
        setOptions(options);
        if (!options.isEmpty()) {
            setValue(options.get(0));
        }
    }

    /**
     * Sets the available page size options.
     *
     * @param options List of page sizes (-1 represents "All")
     */
    public void setOptions(List<Integer> options) {
        elemental.json.JsonArray jsonArray = elemental.json.Json.createArray();
        for (int i = 0; i < options.size(); i++) {
            jsonArray.set(i, options.get(i));
        }
        getElement().setPropertyJson("options", jsonArray);
    }

    /**
     * Sets the currently selected page size.
     */
    public void setValue(int value) {
        getElement().setProperty("value", value);
    }

    /**
     * Gets the currently selected page size.
     */
    public int getValue() {
        return getElement().getProperty("value", 50);
    }

    /**
     * Sets the label displayed before the dropdown.
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label);
    }

    /**
     * Sets the label for the "All" option (-1 value).
     */
    public void setAllLabel(String label) {
        getElement().setProperty("allLabel", label);
    }

    /**
     * Sets the localStorage key for persisting the selection.
     */
    public void setStorageKey(String key) {
        getElement().setProperty("storageKey", key);
    }

    /**
     * Sets the theme (dark/light).
     */
    public void setTheme(String theme) {
        getElement().setAttribute("theme", theme);
    }

    /**
     * Adds a listener for page size changes.
     */
    public void addPageSizeChangeListener(ComponentEventListener<PageSizeChangeEvent> listener) {
        addListener(PageSizeChangeEvent.class, listener);
    }

    @DomEvent("page-size-change")
    public static class PageSizeChangeEvent extends ComponentEvent<BervanPageSizeSelector> {
        private final int value;

        public PageSizeChangeEvent(BervanPageSizeSelector source, boolean fromClient,
                                   @EventData("event.detail.value") int value) {
            super(source, fromClient);
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
