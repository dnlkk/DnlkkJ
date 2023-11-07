package com.dnlkk;

import java.lang.reflect.InvocationTargetException;

import com.dnlkk.boot.Banner;
import com.dnlkk.controller.ControllerRegistry;
import com.dnlkk.controller.JspDispatcherServlet;
import com.dnlkk.dependency_injector.annotations.components.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.boot.AppConfig;
import com.dnlkk.boot.annotations.DnlkkWeb;
import com.dnlkk.controller.ApiDispatcherServlet;
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

            ApplicationContext applicationContext = applicationContextClass.getConstructor(Object.class).newInstance(app);

            if (this.primarySource.isAnnotationPresent(DnlkkWeb.class)) {
                ControllerRegistry controllerRegistry = new ControllerRegistry();
                ApiDispatcherServlet apiDispatcherServlet = new ApiDispatcherServlet(controllerRegistry);
                JspDispatcherServlet jspDispatcherServlet = new JspDispatcherServlet(controllerRegistry);
                applicationContext.getComponents().values().forEach(component -> {
                    if (component.getClass().isAnnotationPresent(RestController.class))
                        controllerRegistry.registerController(component.getClass().getAnnotation(RestController.class).value(), component);
                    if (component.getClass().isAnnotationPresent(Controller.class))
                        controllerRegistry.registerController(component.getClass().getAnnotation(Controller.class).value(), component);
                });
                new FrontController(apiDispatcherServlet, jspDispatcherServlet);
            }

            return primarySource.cast(app);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            logger.error(e.getMessage());
            logger.error("Project initialize failed! Exit...");
            System.exit(-1);
        }
        return null;
    }

    public DnlkkApplication(Class<?> clazz) {
        isApplicationConfigLoaded = AppConfig.loadConfig(clazz);

        try {
            banner = Banner.init(clazz);
        } catch (Exception e) {
            banner = Banner.init(null);
        }
        if (banner != null)
            System.out.println(banner);
        this.primarySource = clazz;
        this.applicationContextClass = AnnotationApplicationContext.class;
    }


}