package com.bervan.common.component;

public enum BervanButtonStyle {
    PRIMARY("option-button-primary"), SECONDARY("option-button-secondary"), WARNING("option-button-warning");

    private final String className;

    BervanButtonStyle(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
