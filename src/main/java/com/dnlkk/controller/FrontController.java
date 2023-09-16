package com.dnlkk.controller;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.boot.AppConfig;

import lombok.Data;

@Data
public class FrontController {
    private final Logger logger = LoggerFactory.getLogger(FrontController.class);
    private final DispatcherServlet dispatcherServlet;
    private Tomcat tomcat;

    public FrontController(DispatcherServlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
        System.out.println("in front controller!");
        System.out.println(this.dispatcherServlet.getControllerRegistry().getControllers());
        System.out.println(dispatcherServlet.getControllerRegistry().getControllers());
        
        Tomcat tomcat = new Tomcat();
        String portProperty = AppConfig.getProperty("app.port");
        Integer port = portProperty == null ? 8080 : Integer.parseInt(portProperty);

        String hostname = AppConfig.getProperty("app.hostname");
        tomcat.setPort(port);
        tomcat.setHostname(hostname != null ? hostname : "localhost");

        Context context = tomcat.addContext("", null);

        // Создайте сервлет и добавьте его в контекст
        Tomcat.addServlet(context, "DispatcherServlet", this.dispatcherServlet);
        context.addServletMappingDecoded("/*", "DispatcherServlet");
        this.tomcat = tomcat;
        this.tomcat.getConnector();
        try {
            this.tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
        this.tomcat.getServer().await();
    }
}