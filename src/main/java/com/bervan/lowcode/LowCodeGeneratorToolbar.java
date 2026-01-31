package com.bervan.lowcode;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.table.BervanFloatingToolbar;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class LowCodeGeneratorToolbar extends BervanTableToolbar<UUID, LowCodeClass> {
    private BervanButton runGeneratorButton;
    private BervanFloatingToolbar floatingToolbar;

    public LowCodeGeneratorToolbar(List<Checkbox> checkboxes, List<LowCodeClass> data, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange
            , BervanViewConfig bervanViewConfig, Function<Void, Void> refreshDataFunction, BaseService<UUID, LowCodeClass> service) {
        super(checkboxes, data, LowCodeClass.class, bervanViewConfig, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, refreshDataFunction, service);
    }

    /**
     * Sets the floating toolbar to add custom actions to.
     * Also enables icon buttons for modern UI consistency.
     */
    public LowCodeGeneratorToolbar withFloatingToolbar(BervanFloatingToolbar floatingToolbar) {
        this.floatingToolbar = floatingToolbar;
        this.useIconButtons = true; // Enable icon buttons for consistency
        return this;
    }

    public LowCodeGeneratorToolbar withRunGenerator() {
        runGeneratorButton = new BervanButton(new Icon(VaadinIcon.COG), ev -> {
            handleRunGenerator();
        });
        runGeneratorButton.getElement().setAttribute("title", "Run generator");
        runGeneratorButton.addClassName("bervan-icon-btn");
        runGeneratorButton.addClassName("primary");

        actionsToBeAdded.add(runGeneratorButton);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "run-generator",
                    "vaadin:cog",
                    "Run generator",
                    "primary",
                    event -> handleRunGenerator()
            );
        }

        return this;
    }

    private void handleRunGenerator() {
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

            ((LowCodeClassService) service).generateCode(toSet);

            checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
            selectAllCheckbox.setValue(false);
            showPrimaryNotification("Code Generation ended!");
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }
}
