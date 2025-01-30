package com.bervan.common;

import com.vaadin.flow.component.button.Button;

public class BervanButton extends Button {
    private final String className = "option-button";

    public BervanButton(String textValue) {
        this.setText(textValue);
        addClassName(className);
    }

    public BervanButton(String textValue, boolean initialVisibility) {
        this(textValue);
        setVisible(initialVisibility);
    }
}
