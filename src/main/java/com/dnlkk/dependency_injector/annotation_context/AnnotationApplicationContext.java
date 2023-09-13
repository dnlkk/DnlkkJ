package com.dnlkk.dependency_injector.annotation_context;

import com.dnlkk.dependency_injector.application_context.ApplicationContext;

public class AnnotationApplicationContext extends ApplicationContext{
    public AnnotationApplicationContext(Object baseObject) {
        super(
            baseObject, 
            new AnnotationPeaFactory(), 
            new AnnotationConfigScanner(),
            new AnnotationComponentFactory()
            );
        componentFactory.setDependencyInjector(dependencyInjector);
    }
}