package com.dnlkk.dependency_injector.annotation_context;

import com.dnlkk.dependency_injector.application_context.ApplicationContext;

public class AnnotationApplicationContext extends ApplicationContext{
    public AnnotationApplicationContext(String basePackage) {
        super(
            basePackage, 
            new AnnotationPeaFactory(), 
            new AnnotationConfigScanner()
            );
    }
}