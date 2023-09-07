package com.dnlkk.dependency_injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.dnlkk.dependency_injector.config.DependencyInjectionContainer;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.annotations.AutoInject;

public class DependencyInjector {
    private DependencyInjectionContainer componentFactory;

    public DependencyInjector(DependencyInjectionContainer componentFactory) {
        this.componentFactory = componentFactory;
    }

    private boolean fieldSet(Object target, Object dependency, Field field) throws IllegalArgumentException, IllegalAccessException{
        if (dependency != null) {
            this.inject(dependency);
            field.setAccessible(true);
            field.set(target, dependency);
            return true;
        }
        return false;
    }

    public void inject(Object target) {
        Class<?> targetClass = target.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ConcreteInject.class)) {
                try {
                    Class<?> fieldType = field.getType();
                    Object dependency;
                    if (field.isAnnotationPresent(Prototype.class)) {
                        dependency = componentFactory.getPrototype(fieldType);
                        if (dependency == null)
                            dependency = componentFactory.getPrototype(fieldType, field.getAnnotation(ConcreteInject.class).injectName());
                    }
                    else {
                        dependency = componentFactory.getSingleton(fieldType, field.getAnnotation(ConcreteInject.class).injectName());
                        if (dependency == null)
                            dependency = componentFactory.getSingleton(fieldType);
                    }
                    this.fieldSet(target, dependency, field);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else if (field.isAnnotationPresent(AutoInject.class)) {
                try {
                    Class<?> fieldType = field.getType();
                    Object dependency;
                    if (field.isAnnotationPresent(Prototype.class)) {
                            dependency = componentFactory.getPrototype(fieldType);
                        if (dependency == null)
                            dependency = componentFactory.getPrototype(fieldType, field.getName());
                    }
                    else {
                        dependency = componentFactory.getSingleton(fieldType);
                        if (dependency == null)
                            dependency = componentFactory.getSingleton(fieldType, fieldType.getName());
                    }
                    if (this.fieldSet(target, dependency, field))
                        continue;

                    if (fieldType.getDeclaredConstructors().length == 0)
                        throw new NoSuchMethodException(String.format("%s doesn't have @Pea and contructor", field.getName()));
                    Constructor<?> constructor = fieldType.getDeclaredConstructor();
                    if (constructor != null) {
                        dependency = constructor.newInstance();
                        if (this.fieldSet(target, dependency, field))
                            continue;
                    }
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }
}