package com.dnlkk.controller;

import com.dnlkk.doc.DocModelDnlkk;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JspDispatcherServlet extends HttpServlet {
    private final ControllerRegistry controllerRegistry;

    public JspDispatcherServlet(ControllerRegistry controllerRegistry) {
        this.controllerRegistry = controllerRegistry;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getRequestURI().endsWith("/doc.html")) {
            StringBuffer url = request.getRequestURL();

            String fullURL = url.toString();
            String baseURL = fullURL.substring(0, fullURL.lastIndexOf('/')) + "/api";
            DocModelDnlkk docModelDnlkk = new DocModelDnlkk(baseURL, controllerRegistry.getControllers());
            request.setAttribute("model", docModelDnlkk);
            System.out.println(docModelDnlkk);

            request.getRequestDispatcher(replaceHtmlWithJsp(request.getRequestURI())).forward(request, response);
        }
    }
    public static String replaceHtmlWithJsp(String input) {
        return input.replaceAll("\\.html$", ".jsp");
    }
}
