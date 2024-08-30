package com.bervan.common.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface VaadinTableColumn {
    String internalName();

    String displayName();

    String[] strValues() default {};

    boolean valuesSingleChoice() default true;

    int[] intValues() default {};

    int order() default 0;

    boolean inSaveForm() default true;

    boolean inEditForm() default true;
}
