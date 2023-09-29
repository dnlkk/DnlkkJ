package com.dnlkk.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import com.dnlkk.controller.annotations.*;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.util.ControllerUtils;
import com.dnlkk.util.EntityUtils;
import com.dnlkk.util.PathUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Getter;

@Getter
public class DispatcherServlet extends HttpServlet {
    private final ControllerRegistry controllerRegistry;

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
            // Если путь не соответствует ни одному из обработчиков, вы можете вернуть
            // код состояния 404 или выполнить другие действия по умолчанию.
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
            path.substring(entryMapController.getKey().length(), path.length());

        Object controller = entryMapController.getValue();
        
        if (controller != null){
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
                                List<Object> parameters = new ArrayList<>();
                                List<String> requestPaths = Arrays.stream(PathUtils.splitPath("/", methodPath)).toList();
                                List<String> requestMappingPaths = Arrays.stream(PathUtils.splitPath("/", requestMapping)).toList();

                                for (Parameter parameter : controllerEndpoint.getParameters()) {
                                    if (parameter.isAnnotationPresent(RequestParam.class)
                                            && parametersMap.containsKey(parameter.getAnnotation(RequestParam.class).value())) {
                                        Object[] params = parametersMap.get(parameter.getAnnotation(RequestParam.class).value())[0].split(",");
                                        if (!parameter.getType().isArray() && params.length == 1)
                                            if (parameter.getType().equals(Integer.class))
                                                parameters.add(Integer.parseInt((String) params[0]));
                                            else
                                                parameters.add(parameter.getType().cast(params[0]));
                                        else {
                                            Object[] returnObject = Arrays.copyOf(params, params.length);
                                            if (parameter.getType().getComponentType().equals(Integer.class)) {
                                                returnObject = new Integer[params.length];
                                                for (int i = 0; i < returnObject.length; i++) {
                                                    returnObject[i] = Integer.parseInt((String) params[i]);
                                                } 
                                            }
                                            parameters.add(returnObject);
                                        }

                                    }
                                    else if (controllerEndpoint.isAnnotationPresent(Post.class)
                                            && parameter.isAnnotationPresent(RequestBody.class)) {
                                        parameters.add(EntityUtils.objectMapper.readValue(
                                                request.getReader().lines().collect(Collectors.joining(System.lineSeparator())),
                                                parameter.getType())
                                        );
                                    }
                                    else if (parameter.isAnnotationPresent(PathVar.class)) {
                                        int index = requestMappingPaths.indexOf(String.format(":%s", parameter.getAnnotation(PathVar.class).value()));
                                        if (index == -1)
                                            continue;
                                        if (parameter.getType().equals(Integer.class))
                                            parameters.add(Integer.parseInt(requestPaths.get(index)));
                                        else
                                            parameters.add(requestPaths.get(index));
                                    }
                                }
                            Object controllerReturn = controllerEndpoint.invoke(controller, parameters.toArray());
                            if (controllerReturn.getClass().equals(ResponseEntity.class)) {
                                ResponseEntity responseEntity = (ResponseEntity) controllerReturn;
                                response.setStatus(responseEntity.getStatus().code());

                                response.getWriter().write(responseEntity.json());
                            } else if (controllerReturn.getClass().equals(String.class)) {
                                response.getWriter().write(controllerReturn.toString());
                            } else {
                                return false;
                            }
                            return true;
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
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
