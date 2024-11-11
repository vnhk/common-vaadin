package com.bervan.common;

import com.bervan.common.model.BaseOneValue;
import com.bervan.common.onevalue.OneValue;
import com.bervan.common.service.BaseOneValueService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;
import java.util.Optional;

public abstract class AbstractOneValueView extends AbstractPageView {
    protected final BaseOneValueService service;
    protected final MenuNavigationComponent pageLayout;
    protected final String key;
    protected BaseOneValue item;
    protected TextArea textArea = new TextArea();

    public AbstractOneValueView(MenuNavigationComponent pageLayout, String key, String headerValue, BaseOneValueService service) {
        this.service = service;
        this.pageLayout = pageLayout;
        this.key = key;
        if (pageLayout != null) {
            add(pageLayout);
        }

        H3 header = new H3(headerValue);

        textArea.setWidthFull();
        textArea.setHeight(getTextAreaHeight());
        Button saveButton = new Button("Save");
        saveButton.addClassName("option-button");

        saveButton.addClickListener(event -> save(textArea.getValue()));

        add(header, textArea, saveButton);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Optional<BaseOneValue> oneValue = load(key);
        item = oneValue.orElseGet(OneValue::new);
        item.setName(key);
        textArea.setValue(item.getContent() == null ? "" : item.getContent()); //load after app initialization
    }

    protected String getTextAreaHeight() {
        return "500px";
    }

    protected Optional<BaseOneValue> load(String key) {
        List<BaseOneValue> list = service.loadByKey(key);
        if (list.size() > 0) {
            return Optional.of(list.get(0));
        }

        return Optional.empty();
    }

    protected void save(String value) {
        item.setContent(value);
        service.save(item);
    }
}