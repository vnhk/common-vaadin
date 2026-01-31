package com.bervan.common.component.table;

import com.bervan.common.view.table.BervanTableConfig;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Java wrapper for the bervan-export-dropdown Lit component.
 * Provides a dropdown menu for exporting table data in various formats.
 */
@Tag("bervan-export-dropdown")
@JsModule("./src/bervan-table/bervan-export-dropdown.ts")
public class BervanExportDropdown extends Component implements HasSize, HasStyle {

    /**
     * Creates a new export dropdown with all formats enabled.
     */
    public BervanExportDropdown() {
        setFormats(Set.of(
                BervanTableConfig.ExportFormat.CSV,
                BervanTableConfig.ExportFormat.EXCEL,
                BervanTableConfig.ExportFormat.JSON
        ));
    }

    /**
     * Creates a new export dropdown with specified formats.
     */
    public BervanExportDropdown(Set<BervanTableConfig.ExportFormat> formats) {
        setFormats(formats);
    }

    /**
     * Sets the available export formats.
     */
    public void setFormats(Set<BervanTableConfig.ExportFormat> formats) {
        elemental.json.JsonArray jsonArray = elemental.json.Json.createArray();
        int i = 0;
        for (BervanTableConfig.ExportFormat format : formats) {
            jsonArray.set(i++, format.name().toLowerCase());
        }
        getElement().setPropertyJson("formats", jsonArray);
    }

    /**
     * Enables or disables the export button.
     */
    public void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

    /**
     * Checks if the export button is disabled.
     */
    public boolean isDisabled() {
        return getElement().getProperty("disabled", false);
    }

    /**
     * Sets the theme (dark/light).
     */
    public void setTheme(String theme) {
        getElement().setAttribute("theme", theme);
    }

    /**
     * Adds a listener for export button clicks.
     */
    public void addExportClickListener(ComponentEventListener<ExportClickEvent> listener) {
        addListener(ExportClickEvent.class, listener);
    }

    @DomEvent("export-click")
    public static class ExportClickEvent extends ComponentEvent<BervanExportDropdown> {
        private final BervanTableConfig.ExportFormat format;

        public ExportClickEvent(BervanExportDropdown source, boolean fromClient,
                                @EventData("event.detail.format") String format) {
            super(source, fromClient);
            this.format = parseFormat(format);
        }

        private BervanTableConfig.ExportFormat parseFormat(String format) {
            if (format == null) {
                return BervanTableConfig.ExportFormat.CSV;
            }
            try {
                return BervanTableConfig.ExportFormat.valueOf(format.toUpperCase());
            } catch (IllegalArgumentException e) {
                return BervanTableConfig.ExportFormat.CSV;
            }
        }

        public BervanTableConfig.ExportFormat getFormat() {
            return format;
        }
    }
}
