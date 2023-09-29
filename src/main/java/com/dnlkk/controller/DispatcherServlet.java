package com.dnlkk.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import com.dnlkk.controller.annotations.*;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.util.ControllerUtils;
import com.dnlkk.util.PathUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class DispatcherServlet extends HttpServlet {
    private final ControllerRegistry controllerRegistry;
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    public DispatcherServlet() {
        controllerRegistry = new ControllerRegistry();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String[]> parameterMap = request.getParameterMap();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String path = request.getPathInfo();
        if (path == null || !dispatch(response, request, parameterMap)) {
            response.setStatus(404);
            response.getWriter().write("{\"error\": \"Ресурс не найден!\"}");
        }
    }

    public boolean dispatch(HttpServletResponse response, HttpServletRequest request, Map<String, String[]> parametersMap) {
        String path = request.getPathInfo();

        Optional<Map.Entry<String, Object>> optionalEntryMapController = controllerRegistry.getControllers().entrySet().stream()
                .filter(entryMapController -> path.startsWith(entryMapController.getKey())).findFirst();

        if (optionalEntryMapController.isEmpty())
            return false;

        Map.Entry<String, Object> entryMapController = optionalEntryMapController.get();

        String methodPath = entryMapController.getKey().length() == path.length()
                ?
                "/"
                :
                path.substring(entryMapController.getKey().length());

        Object controller = entryMapController.getValue();

        if (controller != null) {
            return !Arrays.stream(controller.getClass().getDeclaredMethods())
                    .filter(controllerEndpoint -> {
                        String requestMapping = getRequestMapping(controllerEndpoint);
                        if (requestMapping == null)
                            return false;
                        if (PathUtils.isRequestMapping(methodPath, requestMapping)) {
                            if (ControllerUtils.methodEquals(controllerEndpoint, Get.class, request.getMethod()))
                                return false;
                            else if (ControllerUtils.methodEquals(controllerEndpoint, Post.class, request.getMethod()))
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
                                    if (controllerReturn.getClass().equals(ResponseEntity.class)) {
                                        ResponseEntity<?> responseEntity = (ResponseEntity<?>) controllerReturn;
                                        response.setStatus(responseEntity.getStatus().code());

                                        response.getWriter().write(responseEntity.json());
                                    } else if (controllerReturn.getClass().equals(String.class)) {
                                        response.getWriter().write(controllerReturn.toString());
                                    } else {
                                        return false;
                                    }
                                    return true;
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                                         IOException e) {
                                    logger.error(e.getMessage());
                                }
                        }
                        return false;
                    }).toList().isEmpty();
        }
        return false;
    }

    private String getRequestMapping(Method controllerEndpoint) {
        if (controllerEndpoint.isAnnotationPresent(RequestMapping.class))
            return controllerEndpoint.getAnnotation(RequestMapping.class).value();
        return null;
    }
}
