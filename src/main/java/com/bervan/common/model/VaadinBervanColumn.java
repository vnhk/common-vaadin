package com.bervan.common.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface VaadinBervanColumn {
    String internalName();

    String displayName();

    String[] strValues() default {}; //todo change to class (Constants.class) and field name to get all values from that class

    int[] intValues() default {};

    int order() default 0;

    boolean inSaveForm() default true;

    boolean isWysiwyg() default false;

    boolean inEditForm() default true;

    String defaultValue() default "";

    Class<?> extension() default VaadinBervanColumn.class;

    boolean inTable() default true;

    boolean sortable() default true;
}
