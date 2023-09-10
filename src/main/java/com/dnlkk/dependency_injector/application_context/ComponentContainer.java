package com.dnlkk.dependency_injector.application_context;

public interface ComponentContainer {
    boolean containsComponent(String componentClass);
    Object getComponent(String componentClass);
}