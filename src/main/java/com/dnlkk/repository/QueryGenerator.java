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
        StringBuilder query = new StringBuilder();

        List<String> ignoredFields = null;
        if (args.length > 1)
            ignoredFields = new ArrayList<>(Arrays.stream((String[]) args[args.length - 1]).toList());

        Pageable pageable = Arrays.stream(args)
                .filter(arg -> arg.getClass().equals(Pageable.class))
                .findFirst()
                .map(Pageable.class::cast)
                .orElse(null);

        logger.debug(Arrays.toString(methodParts));

        if (methodParts.length > 0) {
            if (methodParts[0].equals(QueryOperation.FIND.getValue())) {
                query.append("SELECT ").append(tableName).append(".*");
                query.append(getReferencesAs(references, ignoredFields));
            } else if (methodParts[0].equals(QueryOperation.COUNT.getValue()))
                query.append("SELECT COUNT( DISTINCT ").append(tableName).append(".").append(EntityUtils.getRelationIdFieldName(valueClass)).append(" ) ");
            else if (methodParts[0].equals(QueryOperation.SUM.getValue()))
                query.append("SELECT SUM( DISTINCT ").append(tableName).append(".").append(methodParts[1].toLowerCase()).append(") ");


            if (pageable != null)
                query.append(String.format(",ROW_NUMBER() OVER (PARTITION BY %1$s.%2$s ORDER BY %1$s.%2$s) AS rn ", tableName, EntityUtils.getRelationIdFieldName(valueClass)));

            query.append("FROM ").append(tableName);
            boolean whereClauseAdded = false;
            boolean inClause = false;

            if (methodParts[0].equals(QueryOperation.FIND.getValue()))
                query.append(getReferencesJoin(references, tableName, ignoredFields));

            Field[] fields = valueClass.getDeclaredFields();

            for (String methodPart : methodParts) {
                String part = methodPart.toLowerCase();

                if (part.equals("ignored"))
                    break;

                switch (part) {
                    case "all" -> {
                        continue;
                    }
                    case "by" -> {
                        if (!whereClauseAdded) {
                            query.append(" WHERE");
                            whereClauseAdded = true;
                        }
                    }
                    case "in" -> {
                        if (!whereClauseAdded) {
                            query.append(" WHERE");
                            whereClauseAdded = true;
                            inClause = true;
                        }
                    }
                    case "or" -> query.append(" OR");
                    case "and" -> query.append(" AND");
                }
                if (whereClauseAdded) {
                    Field fieldWhere = Arrays.stream(fields).filter(field -> field.getName().toLowerCase().startsWith(part)).findFirst().orElse(null);
                    if (fieldWhere != null) {
                        String paramName = EntityUtils.getColumnName(fieldWhere);
                        query.append(" ").append(tableName).append(".").append(paramName);
                        if (inClause)
                            query.append(" = ANY(?) ");
                        else
                            query.append(" = ? ");
                    }
                }
            }

            String resultQuery = query.toString();

            if (pageable != null) {
                StringBuilder stringBuilder = new StringBuilder(" ");
                if (pageable.getSort() != null) {
                    stringBuilder.append(" ORDER BY  ");
                    for (Sort sort : pageable.getSort()) {
                        stringBuilder.append(sort.getBy()).append(" ").append(sort.getHow()).append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }

                resultQuery = String.format("WITH RankedMessages AS (%s %s)" +
                                ", UniqueRankedMessages AS (" +
                                "    SELECT * " +
                                "    FROM RankedMessages" +
                                "    WHERE rn = 1 " +
                                "    LIMIT %d " +
                                "    OFFSET %d " +
                                ") " +
                                "SELECT * " +
                                "FROM RankedMessages " +
                                "WHERE %5$s IN (SELECT %5$s FROM UniqueRankedMessages) "
                        ,
                        resultQuery,
                        stringBuilder,
                        pageable.getLimit(),
                        pageable.getLimit() * pageable.getPage() + pageable.getOffset(),
                        EntityUtils.getRelationIdFieldName(valueClass));

            }
            logger.debug(resultQuery);
            return resultQuery;
        }
        return null;
    }

    public static String getReferencesAs(List<Field> references, List<String> ignoredFields) {
        StringBuilder builder = new StringBuilder(" ");
        List<String> includedTableNames = new ArrayList<>();
        if (!references.isEmpty()) {

            for (Field field : references) {
                if (ignoredFields.contains(field.getName()))
                    continue;
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

    public static String getReferencesJoin(List<Field> references, String tableName, List<String> ignoredFields) {
        StringBuilder builder = new StringBuilder(" ");
        Set<String> includedTableNames = new HashSet<>();
        if (!references.isEmpty()) {
            for (Field field : references) {
                if (ignoredFields.contains(field.getName()))
                    continue;
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
                } else {
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