package com.dnlkk.dependency_injector.application_context;

import java.util.Map;

public interface PeaFactory {
    <T> T getSingletonPea(Class<T> componentClass, String name);
    <T> T getSingletonPea(Class<T> componentClass);

    <T> T getPrototypePea(Class<T> componentClass, String name);
    <T> T getPrototypePea(Class<T> componentClass);

    <V> Map<String, V> getPeas();
    <V> void setPeas(Map<String, V> peas);
}