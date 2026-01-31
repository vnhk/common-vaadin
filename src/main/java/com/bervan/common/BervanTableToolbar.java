package com.bervan.common;

import com.bervan.common.component.*;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.service.BaseService;
import com.bervan.common.view.AbstractDataIEView;
import com.bervan.common.view.AbstractPageView;
import com.bervan.encryption.EncryptionService;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BervanTableToolbar<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractPageView {
    protected final List<Checkbox> checkboxes;
    protected final List<T> data;
    protected final Class<?> tClass;
    protected final BervanViewConfig bervanViewConfig;
    protected final Function<Void, Void> refreshDataFunction;
    protected final BaseService<ID, T> service;
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");
    protected Checkbox selectAllCheckbox;
    protected Button checkboxDeleteButton;
    protected Button checkboxExportButton;
    protected Button checkboxEditButton;
    protected HorizontalLayout content = new HorizontalLayout();
    protected List<Button> buttonsForCheckboxesForVisibilityChange;
    protected List<Button> actionsToBeAdded = new ArrayList<>();
    protected ComponentHelper<ID, T> componentHelper;
    @Getter
    protected EditItemDialog<ID, T> editItemDialog;
    /**
     * Whether to use icon-only buttons instead of text buttons.
     */
    protected boolean useIconButtons = false;

    public BervanTableToolbar(List<Checkbox> checkboxes,
                              List<T> data, Class<T> tClass, BervanViewConfig bervanViewConfig,
                              Checkbox selectAllCheckbox,
                              List<Button> buttonsForCheckboxesForVisibilityChange,
                              Function<Void, Void> refreshDataFunction,
                              BaseService<ID, T> service) {
        this.checkboxes = (checkboxes);
        this.data = data;
        this.service = service;
        this.bervanViewConfig = bervanViewConfig;
        this.selectAllCheckbox = selectAllCheckbox;
        this.buttonsForCheckboxesForVisibilityChange = buttonsForCheckboxesForVisibilityChange;
        this.tClass = tClass;
        this.refreshDataFunction = refreshDataFunction;
        addClassName("checkbox-actions-bar");
        add(content);

        componentHelper = new CommonComponentHelper<>(tClass);
    }

    /**
     * Enables icon-only mode for a more compact, modern appearance.
     * Called automatically when floating toolbar is enabled.
     */
    public BervanTableToolbar<ID, T> withIconButtons() {
        this.useIconButtons = true;
        return this;
    }

    public BervanTableToolbar<ID, T> build() {
        for (Button action : actionsToBeAdded) {
            content.add(action);
            buttonsForCheckboxesForVisibilityChange.add(action);
        }

        for (Button button : buttonsForCheckboxesForVisibilityChange) {
            button.setEnabled(false);
        }

        return this;
    }

    public BervanTableToolbar<ID, T> withDeleteButton() {
        if (useIconButtons) {
            checkboxDeleteButton = new BervanButton(new Icon(VaadinIcon.TRASH), deleteEvent -> handleDelete());
            checkboxDeleteButton.getElement().setAttribute("title", "Delete selected");
            checkboxDeleteButton.addClassName("bervan-icon-btn");
            checkboxDeleteButton.addClassName("danger");
        } else {
            checkboxDeleteButton = new BervanButton("Delete", deleteEvent -> handleDelete(), BervanButtonStyle.WARNING);
        }
        checkboxDeleteButton.addClassName("delete-action-button");
        actionsToBeAdded.add(checkboxDeleteButton);
        return this;
    }

    private void handleDelete() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirm Deletion");
        confirmDialog.setText("Are you sure you want to delete the selected items?");

        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(event -> {
            Set<String> itemsId = getSelectedItemsByCheckbox();

            List<T> toBeDeleted = data.stream()
                    .filter(e -> e.getId() != null)
                    .filter(e -> itemsId.contains(e.getId().toString()))
                    .toList();

            for (T item : toBeDeleted) {
                service.deleteById(item.getId()); //for deleting original
            }
            showSuccessNotification("Removed " + toBeDeleted.size() + " items");

            selectAllCheckbox.setValue(false);
            for (Button button : buttonsForCheckboxesForVisibilityChange) {
                button.setEnabled(isAtLeastOneCheckboxSelected());
            }

            refreshDataFunction.apply(null);
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }

    public BervanTableToolbar<ID, T> withExportButton(boolean isExportable, BaseService<ID, T> service,
                                                      String pathToFileStorageVal, String globalTmpDirVal) {

        if (useIconButtons) {
            checkboxExportButton = new BervanButton(new Icon(VaadinIcon.DOWNLOAD), exportEvent -> {
                handleExport(isExportable, service, pathToFileStorageVal, globalTmpDirVal);
            });
            checkboxExportButton.getElement().setAttribute("title", "Export selected");
            checkboxExportButton.addClassName("bervan-icon-btn");
            checkboxExportButton.addClassName("success");
        } else {
            checkboxExportButton = new BervanButton("Export", exportEvent -> {
                handleExport(isExportable, service, pathToFileStorageVal, globalTmpDirVal);
            }, BervanButtonStyle.WARNING);
        }

        checkboxExportButton.setVisible(isExportable);
        actionsToBeAdded.add(checkboxExportButton);
        return this;
    }

    private void handleExport(boolean isExportable, BaseService<ID, T> service,
                              String pathToFileStorageVal, String globalTmpDirVal) {
        if (!isExportable) {
            log.error("Table is not exportable!");
            return;
        }

        Set<String> itemsId = getSelectedItemsByCheckbox();

        List<T> toBeExported = data.stream()
                .filter(e -> e.getId() != null)
                .filter(e -> itemsId.contains(e.getId().toString()))
                .toList();

        openExportDialog(toBeExported, service, pathToFileStorageVal, globalTmpDirVal);
    }

    public BervanTableToolbar<ID, T> withEditButton(BaseService<ID, T> service) {
        editItemDialog = new EditItemDialog<>(componentHelper, service, bervanViewConfig);
        editItemDialog.setCustomizePostEditFunction((T item) -> {
            refreshDataFunction.apply(null);
            return item;
        });

        Dialog dialog = new Dialog();
        dialog.setWidth("60vw");

        if (useIconButtons) {
            checkboxEditButton = new BervanButton(new Icon(VaadinIcon.EDIT), editButton -> {
                handleEdit(dialog);
            });
            checkboxEditButton.getElement().setAttribute("title", "Edit selected");
            checkboxEditButton.addClassName("bervan-icon-btn");
            checkboxEditButton.addClassName("primary");
        } else {
            checkboxEditButton = new BervanButton("Edit", editButton -> {
                handleEdit(dialog);
            }, BervanButtonStyle.WARNING);
        }

        actionsToBeAdded.add(checkboxEditButton);
        return this;
    }

    private void handleEdit(Dialog dialog) {
        Set<String> itemsId = getSelectedItemsByCheckbox();
        if (itemsId.size() > 1) {
            log.error("Can't edit more than one item at a time!");
            showErrorNotification("Can't edit more than one item at a time!");
            return;
        }

        T toBeEdited = data.stream()
                .filter(e -> e.getId() != null)
                .filter(e -> itemsId.contains(e.getId().toString()))
                .findFirst().get();

        dialog.removeAll();
        dialog.add(editItemDialog.buildEditItemDialog(dialog, toBeEdited));

        dialog.open();
    }

    public void openExportDialog(List<T> toBeExported, BaseService<ID, T> service, String pathToFileStorageVal, String globalTmpDirVal) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Export");
        dialog.setWidth("80vw");

        HorizontalLayout dialogTopBarLayout = getDialogTopBarLayout(dialog);
        dialog.add(dialogTopBarLayout);

        H4 label = new H4("Enter password to decrypt data. Leave blank to export encrypted data.");
        Input passwordField = new Input();
        passwordField.setType("password");
        passwordField.setPlaceholder("Enter password");
        passwordField.setWidthFull();

        dialog.add(new VerticalLayout(label, passwordField));

        dialog.add(new AbstractDataIEView(service, null, bervanViewConfig, tClass) {
            @Override
            protected void postConstruct() {
                super.upload.setVisible(false);
                super.remove(upload);
                super.filtersLayout.setVisible(false);
                super.remove(filtersLayout);
                super.add(exportButton);
                pathToFileStorage = pathToFileStorageVal;
                globalTmpDir = globalTmpDirVal;
            }

            @Override
            protected List<ExcelIEEntity<?>> getDataToExport() {
                List<ExcelIEEntity<?>> newList = new ArrayList<>();
                for (T t : toBeExported) {
                    if (passwordField.getValue() != null && !passwordField.getValue().isEmpty()) {
                        newList.add((ExcelIEEntity<?>) EncryptionService.decryptAll(t, passwordField.getValue()));
                    } else {
                        newList.add((ExcelIEEntity<?>) t);
                    }
                }

                return newList;
            }
        });

        dialog.open();
    }

    private boolean isAtLeastOneCheckboxSelected() {
        return checkboxes.parallelStream().anyMatch(AbstractField::getValue);
    }

    protected Set<String> getSelectedItemsByCheckbox() {
        return checkboxes.stream()
                .filter(AbstractField::getValue)
                .map(Component::getId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(e -> e.split("checkbox-")[1])
                .collect(Collectors.toSet());
    }
}
