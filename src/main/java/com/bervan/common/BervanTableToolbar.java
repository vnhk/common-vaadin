package com.bervan.common;

import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.service.BaseService;
import com.bervan.common.service.GridActionService;
import com.bervan.common.view.AbstractDataIEView;
import com.bervan.common.view.AbstractPageView;
import com.bervan.core.model.BervanLogger;
import com.bervan.encryption.EncryptionService;
import com.bervan.ieentities.ExcelIEEntity;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BervanTableToolbar<ID extends Serializable, T extends PersistableTableData<ID>> extends AbstractPageView {
    protected final List<Checkbox> checkboxes;
    protected final List<T> data;
    protected final Class<?> tClass;
    protected final GridActionService<ID, T> gridActionService;
    protected Checkbox selectAllCheckbox;
    protected Button checkboxDeleteButton;
    protected Button checkboxExportButton;
    protected HorizontalLayout content = new HorizontalLayout();
    protected List<Button> buttonsForCheckboxesForVisibilityChange;
    protected List<Button> actionsToBeAdded = new ArrayList<>();

    public BervanTableToolbar(GridActionService<ID, T> gridActionService, List<Checkbox> checkboxes,
                              List<T> data, Class<?> tClass,
                              Checkbox selectAllCheckbox,
                              List<Button> buttonsForCheckboxesForVisibilityChange) {
        this.checkboxes = (checkboxes);
        this.gridActionService = gridActionService;
        this.data = data;
        this.selectAllCheckbox = selectAllCheckbox;
        this.buttonsForCheckboxesForVisibilityChange = buttonsForCheckboxesForVisibilityChange;
        this.tClass = tClass;
        addClassName("checkbox-actions-bar");
        add(content);
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
        checkboxDeleteButton = new BervanButton("Delete", deleteEvent -> {
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

                gridActionService.deleteItemsFromGrid(data, toBeDeleted, checkboxes);
                showSuccessNotification("Removed " + toBeDeleted.size() + " items");

                selectAllCheckbox.setValue(false);
                for (Button button : buttonsForCheckboxesForVisibilityChange) {
                    button.setEnabled(isAtLeastOneCheckboxSelected());
                }

                gridActionService.refreshData(data);
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);
        checkboxDeleteButton.addClassName("delete-action-button");
        actionsToBeAdded.add(checkboxDeleteButton);
        return this;
    }

    public BervanTableToolbar<ID, T> withExportButton(boolean isExportable, BaseService<ID, T> service, BervanLogger bervanLogger,
                                                      String pathToFileStorageVal, String globalTmpDirVal) {

        checkboxExportButton = new BervanButton("Export", exportEvent -> {
            if (isExportable) {
                bervanLogger.error("Table is not exportable!");
                return;
            }

            //need to extract items now, to be able to pass them to validation methods
            Set<String> itemsId = getSelectedItemsByCheckbox();

            List<T> toBeExported = data.stream()
                    .filter(e -> e.getId() != null)
                    .filter(e -> itemsId.contains(e.getId().toString()))
                    .toList();

            openExportDialog(toBeExported, service, bervanLogger, pathToFileStorageVal, globalTmpDirVal);
        }, BervanButtonStyle.WARNING);

        checkboxExportButton.setVisible(isExportable);
        actionsToBeAdded.add(checkboxDeleteButton);
        return this;
    }

    protected void openExportDialog(List<T> toBeExported, BaseService<ID, T> service, BervanLogger bervanLogger, String pathToFileStorageVal, String globalTmpDirVal) {
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

        dialog.add(new AbstractDataIEView(service, null, bervanLogger, tClass) {
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
