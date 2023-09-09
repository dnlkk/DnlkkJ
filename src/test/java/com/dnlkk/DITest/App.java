package com.dnlkk.DITest;

import com.dnlkk.dependency_injector.annotation_context.AnnotationApplicationContext;
import com.dnlkk.dependency_injector.application_context.ApplicationContext;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationApplicationContext("com.dnlkk.DITest");
        DnlkkApp dnlkkApp = new DnlkkApp();
        applicationContext.injectDependencies(dnlkkApp);
        
        dnlkkApp.runApp();
    }
}
