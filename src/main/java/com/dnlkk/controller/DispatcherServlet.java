package com.dnlkk.controller;

import com.dnlkk.boot.AppConfig;
import com.dnlkk.controller.annotations.request_method.*;
import com.dnlkk.controller.annotations.RequestMapping;
import com.dnlkk.security.Security;
import com.dnlkk.util.ControllerUtils;
import com.dnlkk.util.PathUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public abstract class DispatcherServlet extends HttpServlet {
    protected final ControllerRegistry controllerRegistry;
    protected final String contextPath = AppConfig.getProperty("app.context-path");
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    public boolean dispatch(HttpServletResponse response, HttpServletRequest request) {
        Map<String, String[]> parametersMap = request.getParameterMap();

        Security.getCorsPolicy().request(request);
        Security.getCorsPolicy().response(response);

        Map.Entry<String, Object> entryMapController = getEntryMapControllerFromRequest(request);

        if (entryMapController == null)
            throw new RuntimeException("Controller doesn't exists!");

        Object controller = entryMapController.getValue();

        String methodPath = getMethodPath(entryMapController.getKey(), request);

        if (controller != null) {
            return Arrays.stream(controller.getClass().getDeclaredMethods())
                    .filter(controllerEndpoint -> {
                        String requestMapping = getRequestMapping(controllerEndpoint);
                        if (requestMapping == null)
                            return false;
                        if (PathUtils.isRequestMapping(methodPath, requestMapping)) {
                            if (ControllerUtils.methodEquals(controllerEndpoint, Get.class, request.getMethod()))
                                return false;
                            else if (ControllerUtils.methodEquals(controllerEndpoint, Post.class, request.getMethod()))
                                return false;
                            else if (ControllerUtils.methodEquals(controllerEndpoint, Delete.class, request.getMethod()))
                                return false;
                            else if (ControllerUtils.methodEquals(controllerEndpoint, Patch.class, request.getMethod()))
                                return false;
                            else if (ControllerUtils.methodEquals(controllerEndpoint, Put.class, request.getMethod()))
                                return false;
                            else try {
                                    String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                                    List<Object> parameters = ControllerUtils.getParametersFromRequest(
                                            controllerEndpoint,
                                            methodPath,
                                            requestMapping,
                                            parametersMap,
                                            body
                                    );
                                    Object controllerReturn = controllerEndpoint.invoke(controller, parameters.toArray());
                                    return controllerDispatch(controllerEndpoint, controllerReturn, request, response);
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                                         IOException e) {
                                    logger.error(e.getMessage());
                                }
                        }
                        return false;
                    }).toList().isEmpty();
        }
        return true;
    }

    protected abstract boolean controllerDispatch(Method controllerEndpoint, Object controllerReturn, HttpServletRequest request, HttpServletResponse response) throws IOException;

    protected String getRequestMapping(Method controllerEndpoint) {
        if (controllerEndpoint.isAnnotationPresent(RequestMapping.class))
            return controllerEndpoint.getAnnotation(RequestMapping.class).value();
        return null;
    }

    protected String getMethodPath(String controllerPath, HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith(contextPath + "/api"))
            path = path.substring(contextPath.length()+4);
        String substring = controllerPath.endsWith(".html") ?
                path.substring(0, path.length()-controllerPath.length())
                :
                path.substring(controllerPath.length());

        return controllerPath.length() == path.length() ?
                "/"
                :
                substring;
    }

    protected Map.Entry<String, Object> getEntryMapControllerFromRequest(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith(contextPath + "/api"))
            path = path.substring(contextPath.length() + 4);

        String finalPath = path;
        Optional<Map.Entry<String, Object>> optionalEntryMapController = controllerRegistry.getControllers().entrySet().stream()
                .filter(entryMapController -> {
                    if (finalPath.endsWith(".html")) // TODO: Don't name your files identically, but in different directories or i'll slap your ass
                        return finalPath.endsWith(entryMapController.getKey());
                    return finalPath.startsWith(entryMapController.getKey());
                }).findFirst();

        if (optionalEntryMapController.isEmpty())
            return null;

        return optionalEntryMapController.get();
    }
}
