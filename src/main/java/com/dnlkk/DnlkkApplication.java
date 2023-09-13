package com.dnlkk;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.boot.AppConfig;
import com.dnlkk.boot.annotations.DnlkkApp;
import com.dnlkk.boot.annotations.DnlkkWeb;
import com.dnlkk.dependency_injector.annotation_context.AnnotationApplicationContext;

public class DnlkkApplication {
    private boolean APPLICATION_CONFIG;
    private String banner = null;
    private Class<?> primarySource;
    private Class<?> applicationContextClass;

    private final Logger logger = LoggerFactory.getLogger(DnlkkApplication.class);

    public static Object run(Class<?> clazz, String[] args) {
        return new DnlkkApplication(clazz).run(args);
    }

    private Object run(String[] args) {
        try {
            return this.applicationContextClass.getConstructor(Object.class).newInstance(primarySource.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.error("Project initialize went wrong! Exit...");
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public DnlkkApplication(Class<?> clazz) {
        APPLICATION_CONFIG = AppConfig.loadConfig();
        if (APPLICATION_CONFIG)
            banner = Banner.init();
        if (banner != null)
            System.out.println(banner);
        this.primarySource = clazz;
        this.applicationContextClass = AnnotationApplicationContext.class;
        if (clazz.getAnnotations().length == 0 || clazz.isAnnotationPresent(DnlkkWeb.class))
            this.applicationContextClass = AnnotationApplicationContext.class;
        if (clazz.isAnnotationPresent(DnlkkApp.class))
            this.applicationContextClass = AnnotationApplicationContext.class;
    }

    
}