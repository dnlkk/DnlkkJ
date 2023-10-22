package com.dnlkk.controller;

import jakarta.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JasperInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.boot.AppConfig;

import lombok.Data;

import java.io.File;
import java.util.Set;

@Data
public class FrontController {
    private final Logger logger = LoggerFactory.getLogger(FrontController.class);
    private final ApiDispatcherServlet apiDispatcherServlet;
    private final JspDispatcherServlet jspDispatcherServlet;
    private Tomcat tomcat;

    public FrontController(ApiDispatcherServlet apiDispatcherServlet, JspDispatcherServlet jspDispatcherServlet) {
        this.apiDispatcherServlet = apiDispatcherServlet;
        this.jspDispatcherServlet = jspDispatcherServlet;

        Tomcat tomcat = new Tomcat();

        String portProperty = AppConfig.getProperty("app.port");
        int port = portProperty == null ? 8080 : Integer.parseInt(portProperty);

        String hostname = AppConfig.getProperty("app.hostname");
        tomcat.setPort(port);
        tomcat.setHostname(hostname != null ? hostname : "localhost");

        File webapp;
        try {
            webapp = new File("src/main/webapp");
        } catch (NullPointerException e) {
            webapp = new File("src/main");
        }

        StandardContext ctx = (StandardContext) tomcat.addWebapp("", webapp.getAbsolutePath());
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/META-INF/resources",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        String contextPath = AppConfig.getProperty("app.context-path");
        if (contextPath == null)
            contextPath = "";
        Tomcat.addServlet(ctx, "DispatcherServlet", this.apiDispatcherServlet);
        ctx.addServletMappingDecoded(contextPath+"/api/*", "DispatcherServlet");
        Tomcat.addServlet(ctx, "JspDispatcherServlet", this.jspDispatcherServlet);
        ctx.addServletMappingDecoded("*.html", "JspDispatcherServlet");

        this.tomcat = tomcat;
        this.tomcat.getConnector();
        try {
            this.tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
        JasperInitializer initializer = new JasperInitializer();
        try {
            initializer.onStartup(null, ctx.getServletContext());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        this.tomcat.getServer().await();
    }
}