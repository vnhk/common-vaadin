package com.bervan.common;

import com.bervan.common.model.BaseOneValue;
import com.bervan.common.service.BaseOneValueService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Optional;

public abstract class AbstractOneValueView extends VerticalLayout {
    protected final BaseOneValueService service;
    protected final AbstractPageLayout pageLayout;
    protected BaseOneValue item;

    public AbstractOneValueView(AbstractPageLayout pageLayout, String key, String headerValue, BaseOneValueService service) {
        this.service = service;
        this.pageLayout = pageLayout;
        add(pageLayout);
        H3 header = new H3(headerValue);

        TextArea textArea = new TextArea();
        textArea.setWidthFull();
        textArea.setHeight("500px");
        Optional<BaseOneValue> oneValue = service.loadByKey(key);
        if(oneValue.isPresent()) {
            item = oneValue.get();
        } else {
            item = createEmpty();
        }
        item.setName(key);
        textArea.setValue(item.getContent() == null ? "" : item.getContent());

        Button saveButton = new Button("Save");
        saveButton.addClickListener(event -> save(textArea.getValue()));

        add(header, textArea, saveButton);
    }

    protected abstract BaseOneValue createEmpty();

    protected void save(String value) {
        item.setContent(value);
        service.save(item);
    }
}