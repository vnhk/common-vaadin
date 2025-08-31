package com.bervan.common;

import com.bervan.encryption.EncryptionService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class WysiwygTextArea extends AbstractPageView implements AutoConfigurableField<String> {
    private final String id;
    private String value;
    private boolean isEncrypted = false;
    private Div editorDiv;
    private boolean viewMode = false;
    private boolean readOnly = false;
    private Button viewEditSwitchButton;
    private Action postClickSwitchAction = () -> {
    };
    private Action onDecryptionSuccessAction = () -> {

    };

    public WysiwygTextArea(String id) {
        this.id = id;
        configure(id, null);
    }

    public WysiwygTextArea(String id, String initValue) {
        this.id = id;
        configure(id, initValue);
    }

    public WysiwygTextArea(String id, String initValue, boolean isViewModeInitial) {
        this.id = id;
        this.viewMode = isViewModeInitial;
        configure(id, initValue);
    }

    public boolean isViewMode() {
        return viewMode;
    }

    private void configure(String id, String initValue) {
        editorDiv = new Div();
        editorDiv.setId(id);
        setStyle(editorDiv);

        setValue(initValue);
        viewEditSwitchButton = new Button();
        viewEditSwitchButton.addClassName("option-button");
        viewEditSwitchButton.addClickListener(click -> {
            this.viewMode = !viewMode;
            executeViewEditModePropertyChange(id);
            setViewEditButtonText();
            this.postClickSwitchAction.run();
        });

        add(viewEditSwitchButton, editorDiv);

        setViewEditButtonText();

        if (isEncrypted) {
            editorDiv.setVisible(false);
            viewEditSwitchButton.setVisible(false);
            showDecryptForm();
        } else {
            attachScripts(id);
        }
    }

    private void attachScripts(String id) {
        getElement().executeJs(
                "var link = document.createElement('link'); " +
                        "link.rel = 'stylesheet'; " +
                        "link.href = 'https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css'; " +
                        "document.head.appendChild(link);"
        );

        getElement().executeJs(
                "var script = document.createElement('script'); " +
                        "script.src = 'https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.js'; " +
                        "script.onload = function() {" +
                        "  var quill = new Quill('#" + id + "', { theme: 'snow' });" +
                        "  quill.on('text-change', function() {" +
                        "    $0.$server.onTextChange(quill.root.innerHTML);" +
                        "  });" +
                        "};" +
                        "document.body.appendChild(script);",
                getElement()
        );

        getElement().executeJs(
                "setTimeout(function() {" +
                        "document.querySelector('#" + id + " .ql-editor').setAttribute('contenteditable', $1);" +
                        "}, 1000)", id, viewMode ? "false" : "true"
        );
    }

    private void executeViewEditModePropertyChange(String id) {
        if (viewMode) {
            getElement().executeJs("document.querySelector('#" + id + " .ql-editor').setAttribute('contenteditable', 'false');");
        } else {
            getElement().executeJs("document.querySelector('#" + id + " .ql-editor').setAttribute('contenteditable', 'true');");
        }
    }

    private void setViewEditButtonText() {
        if (readOnly) {
            viewMode = true;
            viewEditSwitchButton.setVisible(false);
        } else {
            viewEditSwitchButton.setVisible(true);
        }

        if (viewMode) {
            viewEditSwitchButton.setText("Switch to Edit Mode");
        } else {
            viewEditSwitchButton.setText("Switch to View Mode");
        }
    }


    private void setStyle(Div div) {
        div.setWidth("100%");
        div.setHeight("300px");
    }

    @Override
    public void setHeight(String height) {
        editorDiv.setHeight(height);
    }

    @ClientCallable
    public void onTextChange(String text) {
        this.value = text;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String obj) {
        if (readOnly) {
            return;
        }
        isEncrypted = EncryptionService.isEncrypted(obj);
        this.value = obj;

        if (!isEncrypted && editorDiv != null && editorDiv.isVisible()) {
            if (obj != null) {
                editorDiv.getElement().setProperty("innerHTML", obj);
            }
        }
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void setId(String id) {
        super.setId(id); //it sets layout id
    }

    public void setSwitchButtonPostAction(Action postClickSwitchAction) {
        this.postClickSwitchAction = postClickSwitchAction;
    }

    public void setOnDecryptionSuccessAction(Action onDecryptionSuccessAction) {
        this.onDecryptionSuccessAction = onDecryptionSuccessAction;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private void showDecryptForm() {
        VerticalLayout decryptForm = new VerticalLayout();

        H4 itemTitle = new H4("Encrypted Item");
        itemTitle.getStyle().set("color", "var(--lumo-primary-text-color)");

        Input passwordField = new Input();
        passwordField.setType("password");
        passwordField.setPlaceholder("Enter password");
        passwordField.setWidthFull();

        Div messageDiv = new Div();
        messageDiv.getStyle().set("display", "none");

        decryptForm.add(itemTitle, passwordField, messageDiv);

        BervanButton decryptButton = new BervanButton("🔓 Decrypt", e -> {
            String password = passwordField.getValue();
            if (password == null || password.trim().isEmpty()) {
                showErrorNotification("Please enter a password");
                return;
            }

            try {
                String decrypted = EncryptionService.decrypt(this.value, password);
                decryptForm.setVisible(false);
                editorDiv.setVisible(true);

                setValue(decrypted);
                attachScripts(id);

                viewEditSwitchButton.setVisible(true);
                showSuccessNotification("🔓 Item decrypted successfully!");

                onDecryptionSuccessAction.run();
            } catch (Exception ex) {
                showErrorNotification("❌ Wrong password or corrupted data");
                passwordField.clear();
                passwordField.focus();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout(decryptButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        decryptForm.add(buttonsLayout);

        add(decryptForm);
    }
}
