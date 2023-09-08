package com.dnlkk.dependency_injector.annotation_context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dnlkk.dependency_injector.application_context.PeaFactory;
import com.dnlkk.dependency_injector.config.PeaObject;

import lombok.Data;

@Data
public class AnnotationPeaFactory implements PeaFactory {
    private Map<String, PeaObject> peas;

    public AnnotationPeaFactory() {
        this.peas = new HashMap<>();
    }

    @Override
    public <V> void setPeas(Map<String, V> peas) {
        this.peas = (Map<String, PeaObject>) peas;
    }

    @Override
    public <T> T getPrototypePea(Class<T> componentClass, String name) {
        if (peas.containsKey(name)) {
            PeaObject peaObject = peas.get(name);
            return componentClass.cast(invokePeaMethod(peaObject.getConfigInstance(), peaObject.getInvokeMethod()));
        }
        return null;
    }

    @Override
    public <T> T getPrototypePea(Class<T> componentClass) {
        for (PeaObject peaObject : peas.values()) {
            if (peaObject.getSingleton().getClass() == componentClass) 
                return componentClass.cast(invokePeaMethod(peaObject.getConfigInstance(), peaObject.getInvokeMethod()));
        }
        return null;
    }

    @Override
    public <T> T getSingletonPea(Class<T> componentClass, String name) {
        if (peas.containsKey(name)) {
            return componentClass.cast(peas.get(name).getSingleton());
        }
        return null;
    }

    @Override
    public <T> T getSingletonPea(Class<T> componentClass) {
        for (PeaObject peaObject : peas.values()) {
            if (peaObject.getSingleton().getClass() == componentClass) 
                return componentClass.cast(peaObject.getSingleton());
        }
        return null;
    }

    private Object invokePeaMethod(Object configInstance, Method method) {
        try {
            return method.invoke(configInstance);
        } catch (IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to invoke Pea method " + method.getName());
        }
    }
}