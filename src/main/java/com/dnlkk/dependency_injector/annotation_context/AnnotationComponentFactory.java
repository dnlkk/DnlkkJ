package com.dnlkk.dependency_injector.annotation_context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.dependency_injector.annotations.components.Component;
import com.dnlkk.dependency_injector.annotations.components.Repository;
import com.dnlkk.dependency_injector.annotations.components.RestController;
import com.dnlkk.dependency_injector.annotations.components.Service;
import com.dnlkk.dependency_injector.application_context.ComponentFactory;

import lombok.Data;

@Data
public class AnnotationComponentFactory implements ComponentFactory {
    private Map<String, Object> components = new HashMap<>();
    private DependencyInjector dependencyInjector;

    @Override
    public void initComponents(String basePackage) {
        String basePackagePath = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = classLoader.getResources(basePackagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if (resource.getProtocol().equals("file")) {
                    File packageDir = new File(resource.getFile());
                    File[] files = packageDir.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.getName().endsWith(".class")) {
                                String className = basePackage + "." + file.getName().replace(".class", "");
                                Class<?> clazz = Class.forName(className);

                                if (isComponentClass(clazz)) {
                                    Object componentInstance = createComponentInstance(clazz);

                                    if (!clazz.isAnnotationPresent(Repository.class))
                                        dependencyInjector.inject(componentInstance);

                                    components.put(clazz.getName(), componentInstance);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to scan for components.");
        }
        System.out.println(components);
    }

    private boolean isComponentClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class)
            || clazz.isAnnotationPresent(RestController.class)
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
}