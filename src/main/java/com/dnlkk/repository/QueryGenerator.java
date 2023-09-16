package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QueryGenerator.class);

    public static String generateQuery(Method method, String tableName, Class<?> valueClass) {
        String methodName = method.getName();
        String[] methodParts = methodName.split("(?=[A-Z])"); // Разбиваем имя метода по заглавным буквам
        StringBuilder query = new StringBuilder("");

        logger.debug(Arrays.toString(methodParts));

        if (methodParts.length > 0) {
            if (methodParts[0].equals(QueryOperation.FIND.getValue()))
                query.append("SELECT * ");
            else if (methodParts[0].equals(QueryOperation.COUNT.getValue()))
                query.append("SELECT COUNT(*) ");
            else if (methodParts[0].equals(QueryOperation.SUM.getValue()))
                query.append("SELECT SUM(" + methodParts[1].toLowerCase() + ") ");

            query.append("FROM " + tableName);
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
                if (whereClauseAdded) {
                    List<Field> list = Arrays.stream(fields).filter(field -> field.getName().toLowerCase().equals(part)).toList();
                    if (!list.isEmpty()) {
                        int methodParameterIndex = Arrays.stream(fields).toList().indexOf(list.get(0));
                        String paramName = valueClass.getDeclaredFields()[methodParameterIndex].getName();
                        query.append(" " + paramName + " = ? ");
                    }
                }
            }

            String resultQuery = query.toString();
            logger.debug(resultQuery);
            return resultQuery;
        }
        return null;
    }
}