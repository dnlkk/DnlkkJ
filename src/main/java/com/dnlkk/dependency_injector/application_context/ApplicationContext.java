package com.dnlkk.dependency_injector.application_context;

import java.util.Map;

import com.dnlkk.dependency_injector.DependencyInjector;

public abstract class ApplicationContext implements PeaFactory {
    protected final String basePackage;
    protected final DependencyInjector dependencyInjector;

    protected final PeaFactory peaFactory;
    protected final ConfigScanner configScanner;
    protected final ComponentFactory componentFactory;

    public ApplicationContext(String basePackage, PeaFactory peaFactory, ConfigScanner configScanner, ComponentFactory componentFactory) {
        dependencyInjector = new DependencyInjector(this);
        this.basePackage = basePackage;

        this.configScanner = configScanner;
        this.peaFactory = peaFactory;
        this.componentFactory = componentFactory;
    }

    public void injectDependencies(Object target) {
        peaFactory.setPeas(this.configScanner.scan(basePackage));
        dependencyInjector.inject(target);
        componentFactory.initComponents(basePackage);
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