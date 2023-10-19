package com.dnlkk.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.boot.AppConfig;

import lombok.Data;

import java.io.File;

@Data
public class FrontController {
    private final Logger logger = LoggerFactory.getLogger(FrontController.class);
    private final DispatcherServlet dispatcherServlet;
    private final JspDispatcherServlet jspDispatcherServlet;
    private Tomcat tomcat;

    public FrontController(DispatcherServlet dispatcherServlet, JspDispatcherServlet jspDispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
        this.jspDispatcherServlet = jspDispatcherServlet;

        String webappDirLocation = "src/main/webapp/";
        Tomcat tomcat = new Tomcat();

        String portProperty = AppConfig.getProperty("app.port");
        Integer port = portProperty == null ? 8080 : Integer.parseInt(portProperty);

        String hostname = AppConfig.getProperty("app.hostname");
        tomcat.setPort(port);
        tomcat.setHostname(hostname != null ? hostname : "localhost");

        // Создайте сервлет и добавьте его в контекст
        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        String contextPath = AppConfig.getProperty("app.context-path");
        if (contextPath == null)
            contextPath = "";
        Tomcat.addServlet(ctx, "DispatcherServlet", this.dispatcherServlet);
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