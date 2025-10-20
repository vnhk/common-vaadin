package com.bervan.lowcode;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.GridActionService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LowCodeGeneratorToolbar extends BervanTableToolbar<UUID, LowCodeClass> {
    private BervanButton runGeneratorButton;

    public LowCodeGeneratorToolbar(GridActionService<UUID, LowCodeClass> gridActionService, List<Checkbox> checkboxes, List<LowCodeClass> data, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange, BervanViewConfig bervanViewConfig) {
        super(gridActionService, checkboxes, data, LowCodeClass.class, bervanViewConfig, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange);
    }

    public LowCodeGeneratorToolbar withRunGenerator() {
        runGeneratorButton = new BervanButton("Run generator", ev -> {
            Set<String> itemsId = getSelectedItemsByCheckbox();
            if (itemsId.size() > 1) {
                showErrorNotification("Can't run generator for more than one item at a time!");
                return;
            }
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm");
            confirmDialog.setText("Are you sure you want to generate code for the selected item?");

            confirmDialog.setConfirmText("Yes");
            confirmDialog.setConfirmButtonTheme("primary");
            confirmDialog.addConfirmListener(event -> {

                LowCodeClass toSet = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .toList().get(0);

                ((LowCodeClassService) gridActionService.service()).generateCode(toSet);

                checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
                selectAllCheckbox.setValue(false);
                showPrimaryNotification("Code Generation ended!");
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        actionsToBeAdded.add(runGeneratorButton);
        return this;
    }
}