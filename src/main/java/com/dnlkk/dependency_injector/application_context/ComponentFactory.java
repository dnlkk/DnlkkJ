package com.dnlkk.dependency_injector.application_context;

import com.dnlkk.dependency_injector.DependencyInjector;

public interface ComponentFactory {
    void initComponents(String basePackage);
    void setDependencyInjector(DependencyInjector dependencyInjector);
}