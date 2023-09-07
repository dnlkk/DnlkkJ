package com.dnlkk.dependency_injector.config;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeaObject {
        private Object singleton;
        private Method invokeMethod;
        private Object configInstance;
    }