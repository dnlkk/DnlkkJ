package com.dnlkk.repository;

import java.lang.reflect.*;
import java.util.*;

import com.dnlkk.repository.annotations.entity.With;
import com.dnlkk.repository.helper.Interval;
import com.dnlkk.repository.helper.Pageable;
import com.dnlkk.repository.helper.Sort;
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

        List<String> ignoredFields = new ArrayList<>();
        if (args.length > 0)
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


            if (pageable != null) {
                query.append(String.format(",ROW_NUMBER() OVER (PARTITION BY %1$s.%2$s ORDER BY %1$s.%2$s) AS rn ", tableName, EntityUtils.getRelationIdFieldName(valueClass)));
            }

            query.append("FROM ").append(tableName);
            boolean whereClauseAdded = false;
            boolean inClause = false;
            boolean onlyOne = false;

            boolean greaterThan = false;
            boolean lessThan = false;

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
                    case "only" -> onlyOne = true;
                    case "by" -> {
                        if (!whereClauseAdded) {
                            query.append(" WHERE");
                            whereClauseAdded = true;
                        }
                    }
                    case "gt" -> greaterThan = true;
                    case "lt" -> lessThan = true;
                    case "interval" -> {
                        Interval interval = (Interval) Arrays.stream(args)
                                .filter(arg -> arg.getClass().equals(Interval.class))
                                .findFirst()
                                .orElse(null);
                        if (interval == null)
                            break;

                        query.deleteCharAt(query.length() - 1);
                        query.append(" CURRENT_TIMESTAMP - INTERVAL '").append(interval.getValue()).append(" ").append(interval.getDate()).append("'");
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
                        else if (greaterThan)
                            query.append(" >= ?");
                        else if (lessThan)
                            query.append(" <= ?");
                        else
                            query.append(" = ? ");
                        lessThan = false;
                        greaterThan = false;
                    }
                }
            }

            if (pageable != null)
                query.append(
                        String.format(
                                " ORDER BY %s.%s%s",
                                tableName,
                                EntityUtils.getRelationIdFieldName(valueClass),
                                getReferencesOrder(references, ignoredFields)
                        )
                );

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

                resultQuery = String.format(
                        "WITH RankedMessages AS (%s)" +
                                ", UniqueRankedMessages AS (" +
                                "    SELECT * " +
                                "    FROM RankedMessages" +
                                "    WHERE rn = 1 " +
                                " %s" +
                                "    LIMIT %d " +
                                "    OFFSET %d " +
                                ") " +
                                "SELECT * FROM(SELECT " +
                                (onlyOne ?
                                        " DISTINCT ON (" +
                                                EntityUtils.getColumnName(EntityUtils.getIdField(valueClass)) +
                                                " ) "
                                        : " ") +
                                " * " +
                                "FROM RankedMessages " +
                                "WHERE %5$s IN (SELECT %5$s FROM UniqueRankedMessages)) Z %6$s",
                        resultQuery,
                        stringBuilder,
                        pageable.getLimit(),
                        pageable.getLimit() * pageable.getPage() + pageable.getOffset(),
                        EntityUtils.getRelationIdFieldName(valueClass),
                        stringBuilder
                );

            }
            logger.debug(resultQuery);
            return resultQuery;
        }
        return null;
    }

    public static String getReferencesOrder(List<Field> references, List<String> ignoredFields) {
        StringBuilder builder = new StringBuilder(" ");
        Set<String> includedTableNames = new HashSet<>();
        if (!references.isEmpty()) {
            for (Field field : references) {
                if (ignoredFields.contains(field.getName()) || field.isAnnotationPresent(With.class))
                    continue;
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
                } else {
                    for (Field targetField : targetClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(With.class))
                            continue;
                        if ((targetField.isAnnotationPresent(OneToOne.class) && targetField.getAnnotation(OneToOne.class).value().equals(field.getName()))
                                || (targetField.isAnnotationPresent(ManyToOne.class) && targetField.getAnnotation(ManyToOne.class).value().equals(field.getName()))) {
                            targetKey = EntityUtils.getColumnName(targetField);
                        }
                    }
                }

                if (targetKey == null)
                    continue;

                if (!includedTableNames.contains(targetTableName))
                    builder.append(",").append(targetTableName).append(".").append(targetKey);
                includedTableNames.add(targetTableName);
            }
        }
        return builder.toString();
    }

    public static String getReferencesAs(List<Field> references, List<String> ignoredFields) {
        StringBuilder builder = new StringBuilder(" ");
        List<String> includedTableNames = new ArrayList<>();
        if (!references.isEmpty()) {
            for (Field field : references) {
                if (ignoredFields.contains(field.getName()))
                    continue;

                if (field.isAnnotationPresent(With.class)) {
                    builder.append(",(").append(field.getAnnotation(With.class).value()).append(") as ").append(EntityUtils.getColumnName(field)).append(" ");
                    continue;
                }

                Class<?> targetClass;

                if (field.getType() == List.class)
                    targetClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                else
                    targetClass = (Class<?>) field.getGenericType();

                String targetTableName = targetClass.getAnnotation(Table.class).value();
                if (includedTableNames.contains(targetTableName))
                    continue;

                for (Field targetField : targetClass.getDeclaredFields()) {
                    if (targetField.isAnnotationPresent(With.class))
                        continue;
                    if (EntityUtils.isNotFK(targetField) || (targetField.isAnnotationPresent(OneToOne.class) && targetField.getAnnotation(OneToOne.class).value().equals(field.getName()))
                            || (targetField.isAnnotationPresent(ManyToOne.class) && targetField.getAnnotation(ManyToOne.class).value().equals(field.getName()))) {
                        String targetKey = EntityUtils.getColumnName(targetField);
                        if (targetKey == null)
                            continue;

                        builder.append(",").append(targetTableName).append(".").append(targetKey).append(" AS ").append(targetTableName).append("_").append(targetKey).append(" ");
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
                if (ignoredFields.contains(field.getName()) || field.isAnnotationPresent(With.class))
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
                        if (field.isAnnotationPresent(With.class))
                            continue;
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