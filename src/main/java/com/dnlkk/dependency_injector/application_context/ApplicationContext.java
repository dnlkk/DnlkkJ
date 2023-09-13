package com.dnlkk.dependency_injector.application_context;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.dependency_injector.DependencyInjector;

public abstract class ApplicationContext implements PeaFactory, ComponentContainer {
    protected final String basePackage;
    protected final DependencyInjector dependencyInjector;

    protected final PeaFactory peaFactory;
    protected final ConfigScanner configScanner;
    protected final ComponentFactory componentFactory;
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    public ApplicationContext(Object baseObject, PeaFactory peaFactory, ConfigScanner configScanner, ComponentFactory componentFactory) {
        String basePackage = baseObject.getClass().getPackageName();

        this.dependencyInjector = new DependencyInjector(this);
        this.basePackage = basePackage;

        this.configScanner = configScanner;
        this.peaFactory = peaFactory;
        this.componentFactory = componentFactory;
        this.componentFactory.setDependencyInjector(dependencyInjector);
        this.injectDependencies(baseObject);
        Arrays.stream(baseObject.getClass().getMethods()).forEach(method -> {
            if (method.getName() == "runApp")
                try {
                    method.invoke(baseObject);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        });
    }

    public void injectDependencies(Object target) {
        peaFactory.setPeas(this.configScanner.scan(basePackage));
        componentFactory.initComponents(basePackage);
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

    @Override
    public boolean containsComponent(String componentClass) {
        return componentFactory.containsComponent(componentClass);
    }

    @Override
    public Object getComponent(String componentClass) {
        return componentFactory.getComponent(componentClass);
    }
}