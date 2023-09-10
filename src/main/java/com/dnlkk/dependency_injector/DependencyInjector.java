package com.dnlkk.dependency_injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.annotations.lifecycle.Singleton;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.application_context.ApplicationContext;

public class DependencyInjector {
    private final ApplicationContext applicationContext;

    public DependencyInjector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static boolean setField(Object targetObject, Object dependencyInstance, Field field) {
        if (dependencyInstance != null) {
            field.setAccessible(true);
            try {
                field.set(targetObject, dependencyInstance);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Object createDependencyInstance(Class<?> fieldType) {
        try {
            Constructor<?> constructor = fieldType.getDeclaredConstructor();
            if (constructor != null) {
                return constructor.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void inject(Object targetObject) {
        Class<?> targetClass = targetObject.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            String injectName = null;
            Object dependencyInstance = null;

            if (field.isAnnotationPresent(ConcreteInject.class)) 
                injectName = field.getAnnotation(ConcreteInject.class).injectName();
            else if (field.isAnnotationPresent(AutoInject.class)){
                System.out.println(fieldType.getSimpleName());
                if (applicationContext.containsComponent(fieldType.getSimpleName()))
                    injectName = fieldType.getSimpleName();
                else
                    injectName = field.getName();
            } else
                continue;
            
            

            if (field.isAnnotationPresent(Prototype.class))
                dependencyInstance = applicationContext.getPrototypePea(fieldType, injectName);
            else{
                if (applicationContext.containsComponent(injectName))
                    dependencyInstance = applicationContext.getComponent(injectName);
                else    
                    dependencyInstance = applicationContext.getSingletonPea(fieldType, injectName);
                System.out.println(dependencyInstance);
                System.out.println(injectName);
                System.out.println(applicationContext.containsComponent(injectName));
            }

            if (dependencyInstance == null && field.isAnnotationPresent(AutoInject.class))
                dependencyInstance = createDependencyInstance(fieldType);

            this.inject(dependencyInstance);
            if (!setField(targetObject, dependencyInstance, field))
                throw new RuntimeException("Dependency injection failed for field: " + field.getName());
        }
    }
}