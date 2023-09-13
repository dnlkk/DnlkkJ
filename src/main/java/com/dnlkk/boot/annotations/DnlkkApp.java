package com.dnlkk.boot.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dnlkk.dependency_injector.config.Config;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DnlkkApp {
}