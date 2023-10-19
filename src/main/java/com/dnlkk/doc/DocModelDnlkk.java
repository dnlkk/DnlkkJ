package com.dnlkk.doc;

import com.dnlkk.controller.annotations.*;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.doc.annotation.ApiOperation;
import com.dnlkk.doc.annotation.Tag;
import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Data
public class DocModelDnlkk {
    private String message;
    private ControllerModel[] controllers;

    public DocModelDnlkk(String message, Map<String, Object> controllers) {
        this.message = message;
        this.controllers = new ControllerModel[controllers.size()];

        int index = 0;
        for (Map.Entry<String, Object> o : controllers.entrySet()) {
            ControllerModel controller = getControllerModel(o);

            for (Method method : o.getValue().getClass().getDeclaredMethods()) {
                ControllerRequestModel controllerRequestModel = new ControllerRequestModel();
                for (Parameter p : method.getParameters()) {
                    if (p.isAnnotationPresent(RequestBody.class))
                        controllerRequestModel.setApiRequest(ControllerRequestModel.castClassToString(p.getType()));
                    else if (p.isAnnotationPresent(RequestParam.class))
                        controllerRequestModel.getRequestParameters().add(String.format(
                                "\"%s\": %s",
                                p.getAnnotation(RequestParam.class).value(),
                                p.getType().getSimpleName()
                        ));
                }
                if (method.isAnnotationPresent(Get.class))
                    controllerRequestModel.setRequestType(RequestType.GET);
                else if (method.isAnnotationPresent(Post.class)) {
                    controllerRequestModel.setRequestType(RequestType.POST);
                }
                else
                    continue;
                if (method.isAnnotationPresent(RequestMapping.class))
                    controllerRequestModel.setMapping(method.getAnnotation(RequestMapping.class).value());
                controllerRequestModel.setApiName(method.getName());
                controllerRequestModel.setApiResponse(ControllerRequestModel.castClassToString(method.getReturnType()));
                if (method.isAnnotationPresent(ApiOperation.class)) {
                    controllerRequestModel.setApiName(String.format("%s (%s)", method.getAnnotation(ApiOperation.class).name(), controllerRequestModel.getApiName()));
                    controllerRequestModel.setApiResponse(ControllerRequestModel.castClassToString(method.getAnnotation(ApiOperation.class).response()));
                }
                controller.requestModels.add(controllerRequestModel);
            }
            this.controllers[index] = controller;
            index++;
        }
    }

    private static ControllerModel getControllerModel(Map.Entry<String, Object> o) {
        ControllerModel controller = new ControllerModel();
        controller.setMapping(o.getKey());

        Object controllerObject = o.getValue();

        controller.setTagName(controllerObject.getClass().getSimpleName());
        if (controllerObject.getClass().isAnnotationPresent(Tag.class)) {
            Tag tag = controllerObject.getClass().getAnnotation(Tag.class);
            controller.setTagName(String.format("%s (%s)", tag.name(), controller.getTagName()));
            controller.setTagDescription(tag.description());
        }
        return controller;
    }

    @Data
    public static class ControllerModel {
        private String mapping;
        private String tagName;
        private String tagDescription;
        private List<ControllerRequestModel> requestModels = new ArrayList<>();
    }

    @Data
    public static class ControllerRequestModel {
        private String mapping;
        private RequestType requestType;
        private String apiName;
        private String apiRequest;
        private List<String> requestParameters = new ArrayList<>();
        private String apiResponse;

        @SneakyThrows
        private static String castClassToString(Class<?> clazz) {
            StringBuilder stringBuilder = new StringBuilder();

            if (clazz.equals(ResponseEntity.class)) {
                return "Got ResponseEntity<?>, please mark method with @ApiOperation(name, response)";
            }

            stringBuilder.append(String.format("%s: {\n", clazz.getSimpleName()));
            for (Field field : clazz.getDeclaredFields()) {
                stringBuilder.append("    ");

                stringBuilder.append(String.format(
                        "\"%s\": %s,\n",
                        field.getName(),
                        Collection.class.isAssignableFrom(field.getType()) ?
                                (field.getType().getSimpleName() + String.format("<%s>", ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]).getSimpleName()))
                                :
                                Map.class.isAssignableFrom(field.getType()) ?
                                        (field.getType().getSimpleName() + String.format("<%s, %s>", ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]).getSimpleName(), ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1]).getSimpleName()))
                                        :
                                        field.getType().getSimpleName()
                ));
            }
            stringBuilder.append("}");

            return stringBuilder.toString();
        }
    }
}
