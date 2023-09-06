package com.dnlkk.dependency_injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.dnlkk.dependency_injector.config.ComponentFactory;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.AutoInject;

public class DependencyInjector {
    private ComponentFactory componentFactory;

    public DependencyInjector(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    public void inject(Object target) {
        Class<?> targetClass = target.getClass();
        Field[] fields = targetClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(ConcreteInject.class)) {
                try {
                    Class<?> fieldType = field.getType();
                    Object dependency = componentFactory.getComponent(fieldType, field.getAnnotation(ConcreteInject.class).className());
                    if (dependency != null) {
                        field.setAccessible(true);
                        field.set(target, dependency);
                        continue;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else if (field.isAnnotationPresent(AutoInject.class)) {
                try {
                    Class<?> fieldType = field.getType();
                    Object dependency = componentFactory.getComponent(fieldType);
                    if (dependency != null) {
                        field.setAccessible(true);
                        field.set(target, dependency);
                        continue;
                    }

                    Constructor<?> constructor = fieldType.getDeclaredConstructor();
                    if (constructor != null) {
                        dependency = constructor.newInstance();
                        field.setAccessible(true);
                        field.set(target, dependency);
                        continue;
                    }
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}