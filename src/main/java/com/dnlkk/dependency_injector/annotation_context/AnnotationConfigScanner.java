package com.dnlkk.dependency_injector.annotation_context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dnlkk.dependency_injector.annotations.Pea;
import com.dnlkk.dependency_injector.application_context.ConfigScanner;
import com.dnlkk.dependency_injector.config.Config;
import com.dnlkk.dependency_injector.config.PeaObject;
import com.dnlkk.util.ScannerUtils;

import lombok.Data;

@Data
public class AnnotationConfigScanner implements ConfigScanner {
    private Set<Class<?>> configClasses;

    public AnnotationConfigScanner() {
        this.configClasses = new HashSet<>();
    }

    @Override
    public Set<Class<?>> findConfigClasses(String basePackage) {
        try {
            for (Class<?> clazz : ScannerUtils.findClassesFromDirectory(basePackage)) {
                if (clazz.isAnnotationPresent(Config.class)) {
                    this.configClasses.add(clazz);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Failed to scan for @Config classes.");
        }
        return configClasses;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException |
                 NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create an instance of " + clazz);
        }
    }

    private Object invokePeaMethod(Object configInstance, Method method) {
        try {
            return method.invoke(configInstance);
        } catch (IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to invoke @Pea method " + method.getName());
        }
    }

    @Override
    public Map<String, PeaObject> scan(String basePackage) {
        this.configClasses = findConfigClasses(basePackage);
        Map<String, PeaObject> peas = new HashMap<>();

        for (Class<?> configClass : configClasses) {
            Object configInstance = createInstance(configClass);

            for (Method method : configClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Pea.class)) {
                    Object peaInstance = invokePeaMethod(configInstance, method);
                    if (peas.containsKey(method.getName()))
                        try {
                            throw new Exception(String.format("@Pea with the title: '%s' should be presented in only one @Config", method.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    else
                        peas.put(method.getName(), new PeaObject(peaInstance, method, configInstance));
                }
            }
        }
        return peas;
    }    
}