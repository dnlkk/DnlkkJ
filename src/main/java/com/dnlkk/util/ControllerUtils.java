package com.dnlkk.util;

import com.dnlkk.controller.annotations.PathVar;
import com.dnlkk.controller.annotations.Post;
import com.dnlkk.controller.annotations.RequestBody;
import com.dnlkk.controller.annotations.RequestParam;
import com.dnlkk.repository.Pageable;
import com.dnlkk.repository.Sort;
import com.dnlkk.controller.annotations.PageableParam;
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
            if (parameter.isAnnotationPresent(PageableParam.class)) {
                Integer limit = null;
                if (parametersMap.containsKey("limit"))
                    limit = Integer.parseInt(parametersMap.get("limit")[0]);
                Integer page = null;
                if (parametersMap.containsKey("page"))
                    page = Integer.parseInt(parametersMap.get("page")[0]);
                Integer offset = null;
                if (parametersMap.containsKey("offset"))
                    offset = Integer.parseInt(parametersMap.get("offset")[0]);
                String sortBy = null;
                if (parametersMap.containsKey("sortBy"))
                    sortBy = parametersMap.get("sortBy")[0];
                String sortHow = null;
                if (parametersMap.containsKey("sortHow"))
                    sortHow = parametersMap.get("sortHow")[0];

                Pageable.PageableBuilder pageableBuilder = Pageable.builder()
                        .limit(limit != null ? limit : 10)
                        .page(page != null ? page : 0)
                        .offset(offset != null ? offset : 0);

                if (sortBy != null)
                    if (sortHow != null) pageableBuilder.sort(new Sort(sortBy, sortHow));
                    else pageableBuilder.sort(new Sort(sortBy));

                parameters.add(pageableBuilder.build());
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                if (parametersMap.containsKey(parameter.getAnnotation(RequestParam.class).value())) {
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
                } else
                    parameters.add(null);
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
