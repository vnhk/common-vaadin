package com.bervan.common.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class InlineEditableField extends Div {

    public enum FieldType {
        TEXT, COMBOBOX, DATE, NUMBER, MULTI_SELECT
    }

    private final String label;
    private final FieldType fieldType;
    private final List<String> comboBoxOptions;
    private final Consumer<Object> onSave;

    private Object currentValue;
    private Div displayContainer;
    private boolean editing = false;

    public InlineEditableField(String label, Object currentValue, FieldType fieldType,
                               List<String> comboBoxOptions, Consumer<Object> onSave) {
        this.label = label;
        this.currentValue = currentValue;
        this.fieldType = fieldType;
        this.comboBoxOptions = comboBoxOptions;
        this.onSave = onSave;

        addClassName("inline-editable-field");
        buildDisplayMode();
    }

    public InlineEditableField(String label, Object currentValue, FieldType fieldType,
                               Consumer<Object> onSave) {
        this(label, currentValue, fieldType, null, onSave);
    }

    private void buildDisplayMode() {
        removeAll();
        editing = false;

        Div wrapper = new Div();
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("field-label");

        Span valueSpan = new Span(formatValue(currentValue));
        valueSpan.addClassName("field-value");
        if (currentValue == null || currentValue.toString().isEmpty()) {
            valueSpan.addClassName("empty");
            valueSpan.setText("—");
        }

        wrapper.add(labelSpan, valueSpan);

        Icon editIcon = VaadinIcon.PENCIL.create();
        editIcon.addClassName("edit-icon");

        displayContainer = new Div(wrapper, editIcon);
        displayContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--bervan-spacing-xs, 4px)")
                .set("width", "100%");

        displayContainer.addClickListener(e -> {
            if (!editing) {
                switchToEditMode();
            }
        });

        add(displayContainer);
    }

    private void switchToEditMode() {
        removeAll();
        editing = true;

        Component editor = createEditor();
        add(editor);
    }

    private Component createEditor() {
        switch (fieldType) {
            case COMBOBOX:
                ComboBox<String> combo = new ComboBox<>(label);
                combo.setItems(comboBoxOptions);
                combo.setValue(currentValue != null ? currentValue.toString() : null);
                combo.setWidthFull();
                combo.setAutoOpen(true);
                combo.addValueChangeListener(e -> {
                    if (e.isFromClient()) {
                        saveAndClose(e.getValue());
                    }
                });
                combo.addBlurListener(e -> {
                    if (editing) {
                        buildDisplayMode();
                    }
                });
                combo.getElement().addEventListener("keydown", event -> buildDisplayMode())
                        .setFilter("event.key === 'Escape'");
                combo.focus();
                return combo;

            case DATE:
                DatePicker datePicker = new DatePicker(label);
                if (currentValue instanceof LocalDateTime) {
                    datePicker.setValue(((LocalDateTime) currentValue).toLocalDate());
                } else if (currentValue instanceof LocalDate) {
                    datePicker.setValue((LocalDate) currentValue);
                }
                datePicker.setWidthFull();
                datePicker.addValueChangeListener(e -> {
                    if (e.isFromClient()) {
                        LocalDate date = e.getValue();
                        saveAndClose(date != null ? date.atStartOfDay() : null);
                    }
                });
                datePicker.addBlurListener(e -> {
                    if (editing) {
                        buildDisplayMode();
                    }
                });
                datePicker.getElement().addEventListener("keydown", event -> buildDisplayMode())
                        .setFilter("event.key === 'Escape'");
                datePicker.focus();
                return datePicker;

            case NUMBER:
                NumberField numberField = new NumberField(label);
                if (currentValue instanceof Number) {
                    numberField.setValue(((Number) currentValue).doubleValue());
                }
                numberField.setWidthFull();
                numberField.addValueChangeListener(e -> {
                    if (e.isFromClient()) {
                        saveAndClose(e.getValue());
                    }
                });
                numberField.addBlurListener(e -> {
                    if (editing) {
                        buildDisplayMode();
                    }
                });
                numberField.getElement().addEventListener("keydown", event -> buildDisplayMode())
                        .setFilter("event.key === 'Escape'");
                numberField.focus();
                return numberField;

            case MULTI_SELECT:
                MultiSelectComboBox<String> multiSelect = new MultiSelectComboBox<>(label);
                if (comboBoxOptions != null) {
                    multiSelect.setItems(comboBoxOptions);
                }
                multiSelect.setAllowCustomValue(true);
                multiSelect.addCustomValueSetListener(e -> multiSelect.select(e.getDetail()));
                multiSelect.setClearButtonVisible(true);
                multiSelect.setWidthFull();
                if (currentValue instanceof Collection<?>) {
                    multiSelect.select(((Collection<?>) currentValue).stream()
                            .map(Object::toString).toList());
                }
                multiSelect.addValueChangeListener(e -> {
                    if (e.isFromClient()) {
                        saveAndClose(new ArrayList<>(e.getValue()));
                    }
                });
                multiSelect.addBlurListener(e -> {
                    if (editing) {
                        saveAndClose(new ArrayList<>(multiSelect.getSelectedItems()));
                    }
                });
                multiSelect.getElement().addEventListener("keydown", event -> buildDisplayMode())
                        .setFilter("event.key === 'Escape'");
                multiSelect.focus();
                return multiSelect;

            case TEXT:
            default:
                TextField textField = new TextField(label);
                textField.setValue(currentValue != null ? currentValue.toString() : "");
                textField.setWidthFull();
                textField.addKeyPressListener(Key.ENTER, e -> saveAndClose(textField.getValue()));
                textField.addBlurListener(e -> {
                    if (editing) {
                        saveAndClose(textField.getValue());
                    }
                });
                textField.getElement().addEventListener("keydown", event -> buildDisplayMode())
                        .setFilter("event.key === 'Escape'");
                textField.focus();
                return textField;
        }
    }

    private void saveAndClose(Object newValue) {
        if (!editing) return;
        this.currentValue = newValue;
        try {
            onSave.accept(newValue);
        } catch (Exception ignored) {
        }
        buildDisplayMode();
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof Collection<?> col) {
            if (col.isEmpty()) return "";
            return col.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d)) {
                return String.valueOf((int) d);
            }
            return String.format("%.1f", d);
        }
        return value.toString();
    }
}
