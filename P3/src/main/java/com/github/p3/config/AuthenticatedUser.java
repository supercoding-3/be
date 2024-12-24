package com.github.p3.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)  // 매개변수에 적용될 어노테이션
@Retention(RetentionPolicy.RUNTIME)  // 런타임에 유지되는 어노테이션
public @interface AuthenticatedUser {
}
