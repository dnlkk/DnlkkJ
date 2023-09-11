package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class QueryGenerator {
    public static String generateQuery(Method method, String tableName, Class<?> valueClass) {
        String methodName = method.getName();
        String[] methodParts = methodName.split("(?=[A-Z])"); // Разбиваем имя метода по заглавным буквам

        System.out.println(Arrays.toString(methodParts));
        if (methodParts.length > 0) {
            StringBuilder query = new StringBuilder("SELECT * FROM " + tableName);
            boolean whereClauseAdded = false;
            
            Field[] fields = valueClass.getDeclaredFields();
            // System.out.println(Arrays.toString(fields));
            
            for (int i = 0; i < methodParts.length; i++) {
                String part = methodParts[i].toLowerCase();
                
                if (part.equals("all")) {
                    continue; // Пропускаем "All"
                }
                else if (part.equals("by")) {
                    if (!whereClauseAdded) {
                        query.append(" WHERE");
                        whereClauseAdded = true;
                    }
                } 
                else if (part.equals("or")) {
                    query.append(" OR");
                } 
                else if (part.equals("and")) {
                    query.append(" AND");
                }
                List<Field> list = Arrays.stream(fields).filter(field -> field.getName().toLowerCase().equals(part)).toList();
                if (list.size() > 0) {
                    int methodParameterIndex = Arrays.stream(fields).toList().indexOf(list.get(0));
                    String paramName = valueClass.getDeclaredFields()[methodParameterIndex].getName();
                    query.append(" " + paramName + " = ?");
                    methodParameterIndex++;
                }
            }

            query.append(";");

            System.out.println(query);
            return query.toString();
        }
        return null;
    }
}