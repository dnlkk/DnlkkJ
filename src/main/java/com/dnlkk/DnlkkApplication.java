package com.dnlkk;

import java.lang.reflect.InvocationTargetException;

import com.dnlkk.boot.Banner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.boot.AppConfig;
import com.dnlkk.boot.annotations.DnlkkApp;
import com.dnlkk.boot.annotations.DnlkkWeb;
import com.dnlkk.controller.DispatcherServlet;
import com.dnlkk.controller.FrontController;
import com.dnlkk.dependency_injector.annotation_context.AnnotationApplicationContext;
import com.dnlkk.dependency_injector.annotations.components.RestController;
import com.dnlkk.dependency_injector.application_context.ApplicationContext;

public class DnlkkApplication {
    private boolean isApplicationConfigLoaded;
    private String banner = null;
    private Class<?> primarySource;
    private Class<? extends ApplicationContext> applicationContextClass;

    private final Logger logger = LoggerFactory.getLogger(DnlkkApplication.class);

    public static Object run(Class<?> clazz, String[] args) {
        return new DnlkkApplication(clazz).run(args);
    }

    private Object run(String[] args) {
        try {
            Object app = primarySource.getConstructor().newInstance();
            ApplicationContext applicationContext = this.applicationContextClass.getConstructor(Object.class).newInstance(app);

            if (this.primarySource.isAnnotationPresent(DnlkkWeb.class)) {
                DispatcherServlet dispatcherServlet = new DispatcherServlet();
                applicationContext.getComponents().values().forEach(component -> {
                    if (component.getClass().isAnnotationPresent(RestController.class))
                        dispatcherServlet.getControllerRegistry().registerController(component.getClass().getAnnotation(RestController.class).value(), component);
                });
                new FrontController(dispatcherServlet);
            }

            return primarySource.cast(app);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.error("Project initialize failed! Exit...");
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public DnlkkApplication(Class<?> clazz) {
        isApplicationConfigLoaded = AppConfig.loadConfig();
        if (isApplicationConfigLoaded)
            banner = Banner.init();
        if (banner != null)
            System.out.println(banner);
        this.primarySource = clazz;
        this.applicationContextClass = AnnotationApplicationContext.class;
        if (clazz.isAnnotationPresent(DnlkkWeb.class))
            this.applicationContextClass = AnnotationApplicationContext.class;
        else if (clazz.getAnnotations().length == 0 || clazz.isAnnotationPresent(DnlkkApp.class))
            this.applicationContextClass = AnnotationApplicationContext.class;
    }

    
}