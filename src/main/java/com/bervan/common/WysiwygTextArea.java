package com.bervan.common;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class WysiwygTextArea extends VerticalLayout implements AutoConfigurableField<String> {
    private String value;
    private final String id;
    private Div editorDiv;
    private boolean viewMode = false;
    private Button viewEditSwitchButton;

    public WysiwygTextArea(String id) {
        this.id = id;
        configure(id, null);
    }

    public WysiwygTextArea(String id, String initValue) {
        this.id = id;
        configure(id, initValue);
    }

    private void configure(String id, String initValue) {
        editorDiv = new Div();

        editorDiv.setId(id);
        if (initValue != null) {
            editorDiv.getElement().setProperty("innerHTML", initValue);
        }

        setStyle(editorDiv);

        viewEditSwitchButton = new Button();
        setViewEditButtonText();
        executeViewEditModePropertyChange(id);

        viewEditSwitchButton.addClickListener(click -> {
            viewMode = !viewMode;
            executeViewEditModePropertyChange(id);
            setViewEditButtonText();
        });

        add(viewEditSwitchButton, editorDiv);

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
    }

    private void executeViewEditModePropertyChange(String id) {
        if (viewMode) {
            getElement().executeJs("document.querySelector('#" + id + " .ql-editor').setAttribute('contenteditable', 'false');");
        } else {
            getElement().executeJs("document.querySelector('#" + id + " .ql-editor').setAttribute('contenteditable', 'true');");
        }
    }

    private void setViewEditButtonText() {
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
        getElement().executeJs("document.querySelector('#" + id + " .ql-editor').innerHTML = '" + value + "'");
        this.value = obj;
    }

    @Override
    public void setWidthFull() {
        super.setWidthFull();
    }

    @Override
    public void setId(String id) {
        super.setId(id); //it sets layout id
    }
}
