package com.dnlkk.util;

import com.dnlkk.controller.annotations.Get;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ControllerUtils {
    public static boolean methodEquals(Method controllerEndpoint, Class<? extends Annotation> methodClass, String requestType) {
        boolean requestEquals = requestType.equals(methodClass.getSimpleName().toUpperCase());
        return ((controllerEndpoint.isAnnotationPresent(methodClass) && !requestEquals) ||
                (!controllerEndpoint.isAnnotationPresent(methodClass) && requestEquals));
    }
}
