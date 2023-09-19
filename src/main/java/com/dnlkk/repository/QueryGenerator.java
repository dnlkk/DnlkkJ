package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.repository.annotations.entity.Table;
import com.dnlkk.util.EntityUtils;

public class QueryGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QueryGenerator.class);

    public static String generateQuery(Method method, String tableName, Class<?> valueClass, List<Field> references) {
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

            query.append(getReferencesJoin(references, tableName));
            
            Field[] fields = valueClass.getDeclaredFields();
            
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
                        query.append(" " + tableName + "." + paramName + " = ? ");
                    }
                }
            }

            String resultQuery = query.toString();
            logger.debug(resultQuery);
            return resultQuery;
        }
        return null;
    }

    public static String getReferencesJoin(List<Field> references, String tableName) {
        StringBuilder builder = new StringBuilder(" ");
        List<String> includedTableNames = new ArrayList<>();
        if (!references.isEmpty()) {
            String sourceKey = EntityUtils.getColumnName(EntityUtils.getIdField(references.get(0).getDeclaringClass()));

            for (Field field : references) {
                Class<?> targetClass = null;
                
                if (field.getType() == List.class)
                    targetClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                else
                    targetClass = (Class<?>) field.getGenericType();

                String targetTableName = targetClass.getAnnotation(Table.class).value();
                
                if (includedTableNames.contains(targetTableName))
                 continue;

                String targetKey = EntityUtils.getRelationIdFieldName(targetClass);

                builder.append("LEFT JOIN " + targetTableName + " ON " + tableName + "."  + sourceKey + " = " + targetTableName + "." + targetKey + " ");
                includedTableNames.add(targetTableName);
            }
        }
        return builder.toString();
    }
}