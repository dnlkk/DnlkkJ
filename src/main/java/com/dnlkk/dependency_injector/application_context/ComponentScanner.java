package com.dnlkk.dependency_injector.application_context;

import com.dnlkk.dependency_injector.DependencyInjector;

public interface ComponentScanner {
    void initComponents(String basePackage);
    void setDependencyInjector(DependencyInjector dependencyInjector);
}