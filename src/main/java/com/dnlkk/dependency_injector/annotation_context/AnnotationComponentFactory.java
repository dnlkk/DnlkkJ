package com.dnlkk.dependency_injector.annotation_context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.dependency_injector.annotations.components.*;
import com.dnlkk.dependency_injector.application_context.ComponentFactory;
import com.dnlkk.repository.DnlkkRepositoryFactory;
import com.dnlkk.util.ScannerUtils;

import lombok.Data;

@Data
public class AnnotationComponentFactory implements ComponentFactory {
    private Map<String, Object> components = new HashMap<>();
    private DependencyInjector dependencyInjector;

    @Override
    public void initComponents(String basePackage) {
        try {
            for (Class<?> clazz : ScannerUtils.findClassesFromDirectory(basePackage)) {
                if (isComponentClass(clazz)) {
                    Object componentInstance = null;

                    if (!clazz.isAnnotationPresent(Repository.class))
                        componentInstance = createComponentInstance(clazz);
                    else
                        componentInstance = DnlkkRepositoryFactory.createRepositoryInstance(clazz);

                    components.put(clazz.getSimpleName(), componentInstance);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Failed to scan for components.");
        }
    }

    private boolean isComponentClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class)
                || clazz.isAnnotationPresent(RestController.class)
                || clazz.isAnnotationPresent(Controller.class)
                || clazz.isAnnotationPresent(Service.class)
                || clazz.isAnnotationPresent(Repository.class);
    }

    private Object createComponentInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create an instance of " + clazz);
        }
    }

    @Override
    public boolean containsComponent(String componentClass) {
        return components.containsKey(componentClass);
    }

    @Override
    public Object getComponent(String componentClass) {
        return components.get(componentClass);
    }
}