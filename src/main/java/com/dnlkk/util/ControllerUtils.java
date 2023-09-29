package com.dnlkk.util;

import com.dnlkk.controller.annotations.PathVar;
import com.dnlkk.controller.annotations.Post;
import com.dnlkk.controller.annotations.RequestBody;
import com.dnlkk.controller.annotations.RequestParam;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ControllerUtils {
    public static boolean methodEquals(Method controllerEndpoint, Class<? extends Annotation> methodClass, String requestType) {
        boolean requestEquals = requestType.equals(methodClass.getSimpleName().toUpperCase());
        return ((controllerEndpoint.isAnnotationPresent(methodClass) && !requestEquals) ||
                (!controllerEndpoint.isAnnotationPresent(methodClass) && requestEquals));
    }

    public static List<Object> getParametersFromRequest(Method controllerEndpoint, String methodPath, String requestMapping, Map<String, String[]> parametersMap, String body) throws JsonProcessingException {
        List<Object> parameters = new ArrayList<>();
        List<String> requestPaths = Arrays.stream(PathUtils.splitPath("/", methodPath)).toList();
        List<String> requestMappingPaths = Arrays.stream(PathUtils.splitPath("/", requestMapping)).toList();

        for (Parameter parameter : controllerEndpoint.getParameters()) {
            if (parameter.isAnnotationPresent(RequestParam.class)
                    && parametersMap.containsKey(parameter.getAnnotation(RequestParam.class).value())) {
                Object[] params = parametersMap.get(parameter.getAnnotation(RequestParam.class).value())[0].split(",");
                if (!parameter.getType().isArray() && params.length == 1)
                    parameters.add(parameterCast(parameter, params[0]));
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

            } else if (controllerEndpoint.isAnnotationPresent(Post.class)
                    && parameter.isAnnotationPresent(RequestBody.class)) {
                parameters.add(EntityUtils.objectMapper.readValue(
                        body,
                        parameter.getType())
                );
            } else if (parameter.isAnnotationPresent(PathVar.class)) {
                int index = requestMappingPaths.indexOf(String.format(":%s", parameter.getAnnotation(PathVar.class).value()));
                if (index == -1)
                    continue;
                if (parameter.getType().equals(Integer.class))
                    parameters.add(Integer.parseInt(requestPaths.get(index)));
                else
                    parameters.add(requestPaths.get(index));
            }
        }
        return parameters;
    }

    public static Object parameterCast(Parameter parameter, Object param) {
        if (parameter.getType().equals(Integer.class))
            return Integer.parseInt((String) param);
        else
            return parameter.getType().cast(param);
    }
}
