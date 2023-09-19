package com.dnlkk.dependency_injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.annotations.lifecycle.Singleton;
import com.dnlkk.boot.AppConfig;
import com.dnlkk.boot.annotations.ConfigValue;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.application_context.ApplicationContext;

public class DependencyInjector {
    private final ApplicationContext applicationContext;

    public DependencyInjector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static boolean setField(Object targetObject, Object dependencyInstance, Field field) {
        if (dependencyInstance == null) {
            return false;
        }
        field.setAccessible(true);
        try {
            field.set(targetObject, dependencyInstance);
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject dependency for field: " + field.getName(), e);
        }
    }

    private Object createDependencyInstance(Class<?> fieldType) {
        try {
            Constructor<?> constructor = fieldType.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create dependency instance for type: " + fieldType.getName(), e);
        }
    }

    public void inject(Object targetObject) {
        Class<?> targetClass = targetObject.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            Object dependencyInstance = null;

            if (field.isAnnotationPresent(ConfigValue.class)) {
                dependencyInstance = AppConfig.getProperty(field.getAnnotation(ConfigValue.class).value());
            } else {
                String injectName = getInjectName(field);
                if (injectName != null)
                    dependencyInstance = resolveDependency(field, injectName);

                if (dependencyInstance == null && field.isAnnotationPresent(AutoInject.class))
                    dependencyInstance = createDependencyInstance(field.getType());
                if (dependencyInstance == null)
                    continue;
                if (dependencyInstance != null)
                    this.inject(dependencyInstance);
            }

            if (!setField(targetObject, dependencyInstance, field)) {
                throw new RuntimeException("Dependency injection failed for field: " + field.getName());
            }
        }
    }

    private String getInjectName(Field field) {
        if (field.isAnnotationPresent(ConcreteInject.class)) {
            return field.getAnnotation(ConcreteInject.class).value();
        } else if (field.isAnnotationPresent(AutoInject.class)) {
            return applicationContext.containsComponent(field.getType().getSimpleName())
                ? field.getType().getSimpleName()
                : field.getName();
        }
        return null;
    }

    private Object resolveDependency(Field field, String injectName) {
        if (injectName == null) {
            return null;
        }

        if (field.isAnnotationPresent(Prototype.class)) {
            return applicationContext.getPrototypePea(field.getType(), injectName);
        } else {
            return applicationContext.containsComponent(injectName)
                ? applicationContext.getComponent(injectName)
                : applicationContext.getSingletonPea(field.getType(), injectName);
        }
    }
}