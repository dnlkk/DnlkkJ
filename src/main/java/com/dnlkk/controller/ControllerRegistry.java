package com.dnlkk.controller;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ControllerRegistry {
    private final Map<String, Object> controllers = new HashMap<>();

    public void registerController(String path, Object controller) {
        if (controllers.containsKey(path))
            throw new RuntimeException("Controller with path " + path + " already exists!");
        else
            controllers.put(path, controller);
        System.out.println(controllers);
    }

    public Object getControllerForPath(String path) {
        return controllers.get(path);
    }
}
