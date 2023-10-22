package com.dnlkk.controller;

import com.dnlkk.controller.annotations.Get;
import com.dnlkk.controller.annotations.Post;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.doc.DocModelDnlkk;
import com.dnlkk.util.ControllerUtils;
import com.dnlkk.util.PathUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class JspDispatcherServlet extends DispatcherServlet {
    public JspDispatcherServlet(ControllerRegistry controllerRegistry) {
        super(controllerRegistry);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getRequestURI().endsWith("/doc.html")) {
            StringBuffer url = request.getRequestURL();

            String fullURL = url.toString();
            String baseURL = fullURL.substring(0, fullURL.lastIndexOf('/')) + contextPath + "/api";
            DocModelDnlkk docModelDnlkk = new DocModelDnlkk(baseURL, controllerRegistry.getControllers());
            request.setAttribute("model", docModelDnlkk);

            request.getRequestDispatcher(replaceHtmlWithJsp(request)).forward(request, response);
        }
        else if (request.getRequestURI().endsWith(".html")) {
            try {
                dispatch(response, request);
            } catch (Exception e) {
                request.setAttribute("model", e.getLocalizedMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
    }
    public String replaceHtmlWithJsp(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        Map.Entry<String, Object> entryMapController = getEntryMapControllerFromRequest(request);
        if (entryMapController != null) {
            Object controller = entryMapController.getValue();

            String methodPath = getMethodPath(entryMapController.getKey(), request);
            Arrays.stream(controller.getClass().getDeclaredMethods())
                    .forEach(controllerEndpoint -> {
                        String requestMapping = getRequestMapping(controllerEndpoint);
                        if (requestMapping == null)
                            return;
                        if (ControllerUtils.methodEquals(controllerEndpoint, Get.class, request.getMethod()))
                            return;
                        else if (ControllerUtils.methodEquals(controllerEndpoint, Post.class, request.getMethod()))
                            return;
                        if (PathUtils.isRequestMapping(methodPath, requestMapping) && stringBuilder.isEmpty())
                            stringBuilder.append(PathUtils.getRequestMapping(methodPath, requestMapping).replace(":", ""));
                    });
        }
        String path = request.getRequestURI();
        if (stringBuilder.isEmpty())
            stringBuilder.append(path);
        else
            stringBuilder.append(path.substring(path.lastIndexOf('/')));

        return stringBuilder.toString().replaceAll("\\.html$", ".jsp");
    }

    @Override
    protected boolean controllerDispatch(Method controllerEndpoint, Object controllerReturn, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (controllerReturn.getClass().equals(ResponseEntity.class)) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) controllerReturn;
            request.setAttribute("model", responseEntity.getBody());
            response.setStatus(responseEntity.getStatus().code());

            try {
                request.getRequestDispatcher(replaceHtmlWithJsp(request)).forward(request, response);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        } else {
            return false;
        }
        return true;
    }
}
