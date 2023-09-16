package com.dnlkk.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.catalina.connector.Response;

import com.dnlkk.controller.annotations.Get;
import com.dnlkk.controller.annotations.Post;
import com.dnlkk.controller.annotations.RequestMapping;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.util.PathUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Data;

@Data
public class DispatcherServlet  extends HttpServlet {
    private final ControllerRegistry controllerRegistry;

    public DispatcherServlet() {
        controllerRegistry = new ControllerRegistry();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        Enumeration<String> a = request.getHeaderNames();
        while( a.hasMoreElements()) {
            String param = a.nextElement();
            System.out.println(String.format("%s = %s", param, request.getHeader(param)));
        }
        Enumeration<String> b = request.getParameterNames();
        while( b.hasMoreElements()) {
            String param = b.nextElement();
            System.out.println(String.format("%s = %s", param, request.getParameter(param)));
        }

        String c = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        System.out.println(c);
        
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        System.out.println(path);

        if (path == null || !dispatch(response, path)) {
            // Если путь не соответствует ни одному из обработчиков, вы можете вернуть
            // код состояния 404 или выполнить другие действия по умолчанию.
            response.setStatus(404);
            response.getWriter().write("{\"error\": \"Ресурс не найден!\"}");
        }
    }

    public boolean dispatch(HttpServletResponse response, String path) {
        
        String[] methodPaths = PathUtils.regexPath("/[a-zA-z]*", path);
        System.out.println(Arrays.toString(methodPaths));
        String methodPath = PathUtils.removeFirstPath(methodPaths, path, 0);
        System.out.println(methodPaths[0]);
        System.out.println(methodPath);

        Object controller = controllerRegistry.getControllerForPath(methodPaths[0]);
        
        if (controller != null){
            return !Arrays.stream(controller.getClass().getMethods())
                .filter(controllerEndpoint -> {
                    if (methodPath.equals(getRequestMapping(controllerEndpoint))) {
                        try {
                            Object controllerReturn = controllerEndpoint.invoke(controller);
                            System.out.println(controllerEndpoint.invoke(controller).toString());
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
