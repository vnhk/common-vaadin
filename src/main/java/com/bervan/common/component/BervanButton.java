package com.bervan.common.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;

public class BervanButton extends Button {
    private final String className = "option-button";

    public BervanButton(String textValue) {
        this.setText(textValue);
        addClassName(className);
    }

    public BervanButton(String textValue, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(textValue, clickListener);
        addClassName(className);
    }

    public BervanButton(String textValue, ComponentEventListener<ClickEvent<Button>> clickListener, BervanButtonStyle bervanButtonStyle) {
        super(textValue, clickListener);
        addClassName(className);
        addClassName(bervanButtonStyle.getClassName());
    }

    public BervanButton(String textValue, boolean initialVisibility) {
        this(textValue);
        setVisible(initialVisibility);
    }

    public BervanButton(Icon icon, ComponentEventListener<ClickEvent<Button>> clickEventComponentEventListener) {
        super(icon, clickEventComponentEventListener);
        addClassName(className);
    }

    public BervanButton() {
        addClassName(className);
    }

    public BervanButton(Icon icon) {
        super(icon);
        addClassName(className);
    }

    public BervanButton(String buttonText, Icon icon) {
        super(buttonText, icon);
    }
}
