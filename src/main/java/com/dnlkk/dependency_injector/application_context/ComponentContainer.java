package com.dnlkk.dependency_injector.application_context;

import java.util.Map;

public interface ComponentContainer {
    boolean containsComponent(String componentClass);
    Object getComponent(String componentClass);
    Map<String, Object> getComponents();
}