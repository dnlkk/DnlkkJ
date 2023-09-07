package com.dnlkk.dependency_injector.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dnlkk.dependency_injector.annotations.Pea;
import com.dnlkk.dependency_injector.annotations.components.RestController;

public class DependencyInjectionContainer {
    private final Map<String, PeaObject> peas = new HashMap<>();
    private final Map<String, Object> components = new HashMap<>();
    private Set<Class<?>> configClasses = new HashSet<>();

    public void scan(String basePackage) {
        this.configClasses = findConfigClasses(basePackage);

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
    }

    public <T> T getPrototype(Class<T> componentClass, String name) {
        if (peas.containsKey(name)) {
            PeaObject peaObject = peas.get(name);
            return componentClass.cast(invokePeaMethod(peaObject.getConfigInstance(), peaObject.getInvokeMethod()));
        }
        return null;
    }

    public <T> T getPrototype(Class<T> componentClass) {
        for (PeaObject peaObject : peas.values()) {
            if (peaObject.getSingleton().getClass() == componentClass) 
                return componentClass.cast(invokePeaMethod(peaObject.getConfigInstance(), peaObject.getInvokeMethod()));
        }
        return null;
    }

    public <T> T getSingleton(Class<T> componentClass, String name) {
        if (peas.containsKey(name)) {
            return componentClass.cast(peas.get(name).getSingleton());
        }
        return null;
    }

    public <T> T getSingleton(Class<T> componentClass) {
        for (PeaObject peaObject : peas.values()) {
            if (peaObject.getSingleton().getClass() == componentClass) 
                return componentClass.cast(peaObject.getSingleton());
        }
        return null;
    }

    private Set<Class<?>> findConfigClasses(String basePackage) {
        String basePackagePath = basePackage.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(basePackagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File packageDir = new File(resource.getFile());
                    File[] files = packageDir.listFiles();
                    // TODO: работать с классами. вытащить из пакета только класса без файлов
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.getName().endsWith(".class")) {
                                String className = basePackage + "." + file.getName().replace(".class", "");
                                Class<?> clazz = Class.forName(className);

                                if (clazz.isAnnotationPresent(Config.class)) {
                                    this.configClasses.add(clazz);
                                }
                                if (clazz.isAnnotationPresent(RestController.class)) {
                                    this.components.put("", clazz);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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
}