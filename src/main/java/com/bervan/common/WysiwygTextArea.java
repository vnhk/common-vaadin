package com.bervan.common;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class WysiwygTextArea extends VerticalLayout implements AutoConfigurableField<String> {
    private String value;
    private final String id;
    private Div editorDiv;

    public WysiwygTextArea(String id) {
        this.id = id;
        configure(id, null);
    }

    public WysiwygTextArea(String id, String initValue) {
        this.id = id;
        configure(id, initValue);
    }

    private void configure(String id, String initValue) {
        //Id in constructor is required to have more than 1 on the same page...
        editorDiv = new Div();
        editorDiv.setId(id);
        if (initValue != null) {
            editorDiv.getElement().setProperty("innerHTML", initValue);
        }

        editorDiv.setWidth("100%");
        editorDiv.setHeight("300px");

        add(editorDiv);

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
