package com.dnlkk.dependency_injector.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dnlkk.dependency_injector.annotations.Pea;

public class ComponentFactory {
    private final Map<Class<?>, List<Object>> components = new HashMap<>();
    private Set<Class<?>> configClasses;
    private final Map<Object, Method[]> methods = new HashMap<>();


    public void scan(String basePackage) {
        this.configClasses = findConfigClasses(basePackage);

        for (Class<?> configClass : configClasses) {
            Object configInstance = createInstance(configClass);

            methods.put(configInstance, configClass.getDeclaredMethods());
            for (Method method : configClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Pea.class)) {
                    Object peaInstance = invokePeaMethod(configInstance, method);
                    Class<?> methodReturnType = method.getReturnType();
                    if (components.containsKey(methodReturnType)) {
                        List<Object> list = components.get(methodReturnType);
                        list.add(peaInstance);
                    }
                    else {
                        List<Object> list = new ArrayList<>();
                        list.add(peaInstance);
                        components.put(method.getReturnType(), list);
                    }
                    System.out.println(method.getReturnType());
                    System.out.println(components.get(method.getReturnType()));
                }
            }
        }
    }

    public <T> T getPrototype(Class<T> componentClass, String name) {
        for (Object configInstance : methods.keySet()) {
            for (Method method : methods.get(configInstance)) {
                if (method.getName().equals(name)){
                    return componentClass.cast(invokePeaMethod(configInstance, method));
                }
            }
        }
        return null;
    }

    public <T> T getPrototype(Class<T> componentClass) {
        for (Object configInstance : methods.keySet()) {
            for (Method method : methods.get(configInstance)) {
                if (method.getReturnType().getName().equals(componentClass.getName()))
                    return componentClass.cast(invokePeaMethod(configInstance, method));
            }
        }
        return null;
    }

    public <T> T getComponent(Class<T> componentClass, String name) {
        return getSingleton(componentClass, name);
    }

    public <T> T getComponent(Class<T> componentClass) {
        return getSingleton(componentClass);
    }

    public <T> T getSingleton(Class<T> componentClass, String name) {
        Object[] objArray = new Object[0];
        if (components.get(componentClass) != null)
        objArray = components.get(componentClass).stream().filter(obj -> {
            System.out.println(name);
            System.out.println(obj.getClass().getSimpleName());
            System.out.println(obj.getClass().getSimpleName().equals(name));
            return obj.getClass().getSimpleName().equals(name);
        }).toArray();

        System.out.println(components.get(componentClass));
        System.out.println(Arrays.toString(objArray));
        if (objArray.length == 0)
            return null;
        Object returnObject = objArray[objArray.length - 1];
        return componentClass.cast(returnObject);
    }

    public <T> T getSingleton(Class<T> componentClass) {
        if (!components.containsKey(componentClass))
            return null;
        List<Object> list = components.get(componentClass);
        Object returnObject = list.get(list.size() - 1);
        return componentClass.cast(returnObject);
    }

    private Set<Class<?>> findConfigClasses(String basePackage) {
        Set<Class<?>> configClasses = new HashSet<>();
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
                                    configClasses.add(clazz);
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