package com.ems.common.annotation;

import com.ems.common.enums.Role;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {
    Role[] value();
}
