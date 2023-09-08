package com.dnlkk.dependency_injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.application_context.ApplicationContext;
import com.dnlkk.dependency_injector.annotations.AutoInject;

@Data
@AllArgsConstructor
public class DependencyInjector {
    private ApplicationContext applicationContext;

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
                        dependency = applicationContext.getPrototypePea(fieldType);
                        if (dependency == null)
                            dependency = applicationContext.getPrototypePea(fieldType, field.getAnnotation(ConcreteInject.class).injectName());
                    }
                    else {
                        dependency = applicationContext.getSingletonPea(fieldType, field.getAnnotation(ConcreteInject.class).injectName());
                        if (dependency == null)
                            dependency = applicationContext.getSingletonPea(fieldType);
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
                            dependency = applicationContext.getPrototypePea(fieldType);
                        if (dependency == null)
                            dependency = applicationContext.getPrototypePea(fieldType, field.getName());
                    }
                    else {
                        dependency = applicationContext.getSingletonPea(fieldType);
                        if (dependency == null)
                            dependency = applicationContext.getSingletonPea(fieldType, fieldType.getName());
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