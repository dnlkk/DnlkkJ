package com.dnlkk.dependency_injector.application_context;

import java.util.Map;
import java.util.Set;

public interface ConfigScanner {
    Set<Class<?>> findConfigClasses(String basePackage);
    <V> Map<String, V> scan(String basePaString);
    Set<Class<?>> getConfigClasses();
}