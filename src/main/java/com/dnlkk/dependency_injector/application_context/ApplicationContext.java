package com.dnlkk.dependency_injector.application_context;

import java.util.Map;

import com.dnlkk.dependency_injector.DependencyInjector;

public abstract class ApplicationContext implements PeaFactory {
    private final PeaFactory peaFactory;
    private final ConfigScanner configScanner;
    private final DependencyInjector dependencyInjector;

    public ApplicationContext(String basePackage, PeaFactory peaFactory, ConfigScanner configScanner) {
        this.configScanner = configScanner;
        this.peaFactory = peaFactory;
        this.peaFactory.setPeas(this.configScanner.scan(basePackage));
        dependencyInjector = new DependencyInjector(this);
    }

    public void injectDependencies(Object target) {
        dependencyInjector.inject(target);
    }

    @Override
    public <T> T getSingletonPea(Class<T> componentClass, String name) {
        return peaFactory.getSingletonPea(componentClass, name);
    }

    @Override
    public <T> T getSingletonPea(Class<T> componentClass) {
        return peaFactory.getSingletonPea(componentClass);
    }

    @Override
    public <T> T getPrototypePea(Class<T> componentClass, String name) {
        return peaFactory.getPrototypePea(componentClass, name);
    }

    @Override
    public <T> T getPrototypePea(Class<T> componentClass) {
        return peaFactory.getPrototypePea(componentClass);
    }

    @Override
    public <V> Map<String, V> getPeas() {
        return peaFactory.getPeas();
    }

    @Override
    public <V> void setPeas(Map<String, V> peas) {
        peaFactory.setPeas(peas);
    }
}