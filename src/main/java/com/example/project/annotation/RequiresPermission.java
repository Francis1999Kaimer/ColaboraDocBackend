package com.example.project.annotation;

import com.example.project.enums.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    

    Permission value();

    String message() default "No tienes permisos para realizar esta operación";
    

    boolean autoDetectProject() default true;

    String projectIdParam() default "";
}
