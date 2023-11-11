package com.dnlkk.controller;

import java.io.IOException;
import java.lang.reflect.Method;

import com.dnlkk.controller.responses.ResponseEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiDispatcherServlet extends DispatcherServlet {
    private static final Logger logger = LoggerFactory.getLogger(ApiDispatcherServlet.class);

    public ApiDispatcherServlet(ControllerRegistry controllerRegistry) {
        super(controllerRegistry);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        if (dispatch(response, request)) {
            response.setStatus(404);
            response.getWriter().write("{\"error\": \"Ресурс не найден!\"}");
        }
    }

    @Override
    protected boolean controllerDispatch(Method controllerEndpoint, Object controllerReturn, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
    }

}
