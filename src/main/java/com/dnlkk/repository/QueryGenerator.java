package com.dnlkk.repository;

import java.lang.reflect.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.repository.annotations.entity.ManyToOne;
import com.dnlkk.repository.annotations.entity.OneToOne;
import com.dnlkk.repository.annotations.entity.Table;
import com.dnlkk.util.EntityUtils;

public class QueryGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QueryGenerator.class);

    public static String generateQuery(Method method, String tableName, Class<?> valueClass, List<Field> references, Object[] args) {
        String methodName = method.getName();
        String[] methodParts = methodName.split("(?=[A-Z])"); // Разбиваем имя метода по заглавным буквам
        StringBuilder query = new StringBuilder("");

        Pageable pageable = Arrays.stream(args)
                .filter(arg -> arg.getClass().equals(Pageable.class))
                .findFirst()
                .map(Pageable.class::cast)
                .orElse(null);

        logger.debug(Arrays.toString(methodParts));

        if (methodParts.length > 0) {
            if (methodParts[0].equals(QueryOperation.FIND.getValue()))
                query.append("SELECT " + tableName + ".*");
            else if (methodParts[0].equals(QueryOperation.COUNT.getValue()))
                query.append("SELECT COUNT( DISTINCT " + EntityUtils.getRelationIdFieldName(valueClass) + " ) ");
            else if (methodParts[0].equals(QueryOperation.SUM.getValue()))
                query.append("SELECT SUM( DISTINCT " + methodParts[1].toLowerCase() + ") ");

            query.append(getReferencesAs(references));

            if (pageable != null)
                query.append(String.format(",ROW_NUMBER() OVER (PARTITION BY %1$s.%2$s ORDER BY %1$s.%2$s) AS rn ", tableName, EntityUtils.getRelationIdFieldName(valueClass)));

            query.append("FROM ").append(tableName);
            boolean whereClauseAdded = false;

            query.append(getReferencesJoin(references, tableName));

            Field[] fields = valueClass.getDeclaredFields();

            for (int i = 0; i < methodParts.length; i++) {
                String part = methodParts[i].toLowerCase();

                if (part.equals("all")) {
                    continue;
                } else if (part.equals("by")) {
                    if (!whereClauseAdded) {
                        query.append(" WHERE");
                        whereClauseAdded = true;
                    }
                } else if (part.equals("or")) {
                    query.append(" OR");
                } else if (part.equals("and")) {
                    query.append(" AND");
                }
                if (whereClauseAdded) {
                    List<Field> list = Arrays.stream(fields).filter(field -> field.getName().toLowerCase().equals(part)).toList();
                    if (!list.isEmpty()) {
                        int methodParameterIndex = Arrays.stream(fields).toList().indexOf(list.get(0));
                        String paramName = valueClass.getDeclaredFields()[methodParameterIndex].getName();
                        query.append(" ").append(tableName).append(".").append(paramName).append(" = ? ");
                    }
                }
            }

            String resultQuery = query.toString();

            if (pageable != null)
                resultQuery = String.format("WITH RankedMessages AS (%s)" +
                        ", UniqueRankedMessages AS (" +
                        "    SELECT * " +
                        "    FROM RankedMessages" +
                        "    WHERE rn = 1 " +
                        "    LIMIT %d " +
                        "    OFFSET %d " +
                        ") " +
                        "SELECT * " +
                        "FROM RankedMessages " +
                        "WHERE %4$s IN (SELECT %4$s FROM UniqueRankedMessages) ",
                        resultQuery,
                        pageable.getLimit(),
                        pageable.getLimit()*pageable.getPage() + pageable.getOffset(),
                        EntityUtils.getRelationIdFieldName(valueClass));

            logger.debug(resultQuery);
            return resultQuery;
        }
        return null;
    }

    public static String getReferencesAs(List<Field> references) {
        StringBuilder builder = new StringBuilder(" ");
        List<String> includedTableNames = new ArrayList<>();
        if (!references.isEmpty()) {

            for (Field field : references) {
                Class<?> targetClass;

                if (field.getType() == List.class)
                    targetClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                else
                    targetClass = (Class<?>) field.getGenericType();

                String targetTableName = targetClass.getAnnotation(Table.class).value();
                if (includedTableNames.contains(targetTableName))
                    continue;

                for (Field targetField : targetClass.getDeclaredFields()) {
                    if (EntityUtils.isNotFK(targetField) || (targetField.isAnnotationPresent(OneToOne.class) && targetField.getAnnotation(OneToOne.class).value().equals(field.getName()))
                            || (targetField.isAnnotationPresent(ManyToOne.class) && targetField.getAnnotation(ManyToOne.class).value().equals(field.getName()))) {
                        String targetKey = EntityUtils.getColumnName(targetField);
                        if (targetKey == null)
                            continue;

                        builder.append(",").append(targetTableName).append(".").append(targetKey).append(" AS ").append(targetTableName).append(targetKey).append(" ");
                    }
                }
                includedTableNames.add(targetTableName);
            }
        }
        return builder.toString();
    }

    public static String getReferencesJoin(List<Field> references, String tableName) {
        System.out.println(references);
        StringBuilder builder = new StringBuilder(" ");
        Set<String> includedTableNames = new HashSet<>();
        if (!references.isEmpty()) {
            for (Field field : references) {
                String sourceKey = EntityUtils.getColumnName(EntityUtils.getIdField(references.get(0).getDeclaringClass()));
                Class<?> targetClass;

                if (field.getType() == List.class)
                    targetClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                else
                    targetClass = (Class<?>) field.getGenericType();

                String targetTableName = targetClass.getAnnotation(Table.class).value();


                String targetKey = null;
                if ((field.isAnnotationPresent(OneToOne.class))
                        || (field.isAnnotationPresent(ManyToOne.class))) {
                    targetKey = EntityUtils.getColumnName(EntityUtils.getIdField(targetClass));
                    sourceKey = EntityUtils.getColumnName(field);
                }
                else {
                    for (Field targetField : targetClass.getDeclaredFields()) {
                        if ((targetField.isAnnotationPresent(OneToOne.class) && targetField.getAnnotation(OneToOne.class).value().equals(field.getName()))
                                || (targetField.isAnnotationPresent(ManyToOne.class) && targetField.getAnnotation(ManyToOne.class).value().equals(field.getName()))) {
                            targetKey = EntityUtils.getColumnName(targetField);
                        }
                    }
                }

                if (targetKey == null)
                    continue;

                if (includedTableNames.contains(targetTableName))
                    builder.append(" OR ").append(tableName).append(".").append(sourceKey).append(" = ").append(targetTableName).append(".").append(targetKey).append(" ");
                else
                    builder.append("LEFT JOIN ").append(targetTableName).append(" ON ").append(tableName).append(".").append(sourceKey).append(" = ").append(targetTableName).append(".").append(targetKey).append(" ");
                includedTableNames.add(targetTableName);
            }
        }
        return builder.toString();
    }
}