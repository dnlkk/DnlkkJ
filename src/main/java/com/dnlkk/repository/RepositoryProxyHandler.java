package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.dnlkk.repository.annotations.entity.*;
import com.dnlkk.util.EntityIgnoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.repository.annotations.Param;
import com.dnlkk.repository.annotations.Query;
import com.dnlkk.repository.dnlkk_connection_pool.DnlkkDataSourceFactory;
import com.dnlkk.util.EntityUtils;
import com.dnlkk.util.SQLQueryUtil;

import lombok.Data;

@Data
public class RepositoryProxyHandler implements InvocationHandler {
    private final DataSource dataSource;
    private String tableName;
    private Class<?> keyClass;
    private Class<?> valueClass;
    private Class<?> repositoryInterface;
    private List<Field> references;
    private final Logger logger = LoggerFactory.getLogger(RepositoryProxyHandler.class);

    public RepositoryProxyHandler(Class<?> clazz) {
        repositoryInterface = clazz;
        this.dataSource = DnlkkDataSourceFactory.createDataSource();
        references = new ArrayList<>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String[] ignoredFields = EntityIgnoreUtils.getIgnoredFieldFromMethod(method, valueClass);

        List<Object> arguments = new ArrayList<>(Arrays.stream(args).toList());
        arguments.add(ignoredFields);
        Object[] args2 = arguments.toArray(new Object[0]);

        Object result = null;
        if (method.isAnnotationPresent(Query.class)) {
            result = executeCustomQuery(method.getAnnotation(Query.class).value(), args2, method.getParameters());
        } else if (method.getName().startsWith(QueryOperation.FIND.getValue())) {
            result = executeFindQuery(method, args2);
        } else if (method.getName().startsWith(QueryOperation.COUNT.getValue()) || method.getName().startsWith(QueryOperation.SUM.getValue())) {
            result = executeCountQuery(method, args2);
        } else if (method.getName().equals(QueryOperation.SAVE.getValue())) {
            result = saveEntity(arguments.get(0));
        }

        if (result != null && !List.class.isAssignableFrom(method.getReturnType()) && (List.class.isAssignableFrom(result.getClass()))) {
            if (((List) result).size() > 0)
                result = ((List) result).get(0);
            else
                result = null;
        }
        return result;
    }

    // TODO: split method
    // voice:.idea/1695822289179.wav
    public List<Object> statementListExecutor(PreparedStatement statement, List<String> ignoredFields) throws SQLException {
        List<Object> resultFunction = new ArrayList<>();
        Object id = null; // TODO: id check
        boolean idChange = false;

        Map<String, Object> relationObjects = new HashMap<>();
        Map<Object, Object> entityToIdMap = new HashMap<>();

        Object entity = null;
        Field[] fields = null;
        try {
            entity = valueClass.getConstructor().newInstance();
            fields = valueClass.getDeclaredFields();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    for (Field field : fields) {
                        if (ignoredFields.contains(field.getName()))
                            continue;
                        if (!EntityUtils.isNotPK(field)) {
                            Object newId = resultSet.getObject(EntityUtils.getColumnName(field));
                            if (id == null || !id.equals(newId)) {
                                id = newId;
                                idChange = true;

                                if (entityToIdMap.containsKey(id)) {
                                    entity = entityToIdMap.get(id);
                                } else {
                                    entity = valueClass.getConstructor().newInstance();
                                    entityToIdMap.put(id, entity);
                                }
                                for (Field field2 : fields) {
                                    if (!EntityUtils.isNotRelation(field2) && relationObjects.get(String.format("%s%s", id.toString(), field2.getName())) == null && !field2.isAnnotationPresent(OneToOne.class) && !field2.isAnnotationPresent(ManyToOne.class))
                                        relationObjects.put(String.format("%s%s", id, field2.getName()), new ArrayList<>());
                                }
                            } else {
                                idChange = false;
                            }
                        }

                        if (EntityUtils.isNotRelation(field)) {
                            Object retrievedObject = resultSet.getObject(EntityUtils.getColumnName(field));
                            DependencyInjector.setField(entity, retrievedObject, field);
                        } else {
                            Class<?> relationClazz = null;
                            if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class))
                                relationClazz = field.getType();
                            else
                                relationClazz = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                            Object relationEntity = relationClazz.getDeclaredConstructor().newInstance();
                            Object relationFromId = null;

                            if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class))
                                DependencyInjector.setField(entity, relationEntity, field);

                            for (Field relationField : relationClazz.getDeclaredFields()) {
                                if (EntityUtils.isNotRelation(relationField)) {
                                    Object retrievedObject = resultSet.getObject(EntityUtils.getTableName(relationField.getDeclaringClass()) + EntityUtils.getColumnName(relationField));
                                    DependencyInjector.setField(relationEntity, retrievedObject, relationField);
                                } else {
                                    try {
                                        relationFromId = resultSet.getObject(EntityUtils.getTableName(relationField.getDeclaringClass()) + EntityUtils.getColumnName(relationField));
                                    } catch (Exception ignored) {
                                        continue;
                                    }

                                    if (relationFromId != null) {
                                        Object relationEntityManyToOne;
                                        if (entityToIdMap.containsKey(relationFromId)) {
                                            relationEntityManyToOne = entityToIdMap.get(relationFromId);
                                        } else {
                                            relationEntityManyToOne = valueClass.getConstructor().newInstance();
                                            for (Field relationEntityField : relationEntityManyToOne.getClass().getDeclaredFields()) {
                                                if (relationEntityField.isAnnotationPresent(PK.class)) {
                                                    relationEntityField.setAccessible(true);
                                                    relationEntityField.set(relationEntityManyToOne, relationFromId);
                                                }
                                            }
                                            entityToIdMap.put(relationFromId, relationEntityManyToOne);
                                        }

                                        DependencyInjector.setField(relationEntity, relationEntityManyToOne, relationField);

                                        String relationKey = String.format("%s%s", relationFromId, field.getName());

                                        if ((field.isAnnotationPresent(OneToOne.class) || (field.isAnnotationPresent(ManyToOne.class))) && relationObjects.get(relationKey) == null) {
                                            relationObjects.put(relationKey, relationEntity);
                                        } else {
                                            if (relationObjects.get(relationKey) == null)
                                                relationObjects.computeIfAbsent(relationKey, k -> new ArrayList<>());
                                            if (((field.isAnnotationPresent(OneToMany.class) && relationField.isAnnotationPresent(ManyToOne.class) && relationField.getAnnotation(ManyToOne.class).value().equals(field.getName())) ||
                                                    (field.isAnnotationPresent(ManyToMany.class)))
                                                    && !((List<Object>) relationObjects.get(relationKey)).contains(relationEntity))
                                                ((List<Object>) relationObjects.get(relationKey)).add(relationEntity);
                                        }
                                    }
                                }
                            }

                            Object list = relationObjects.get(String.format("%s%s", id, field.getName()));

                            DependencyInjector.setField(entity, list, field);
                        }
                    }
                    if (idChange)
                        resultFunction.add(entity);

                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultFunction;
    }


    private Object executeCountQuery(Method method, Object[] args) {
        String sql = QueryGenerator.generateQuery(method, tableName, valueClass, references, args);
        Object result = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = resultSet.getObject(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<Object> executeCustomQuery(String sqlWithParams, Object[] args, Parameter[] parameters) {
        List<Object> result = new ArrayList<>();

        String[] queryParameters = SQLQueryUtil.getParamsFromQuery(sqlWithParams);
        List<String> ignoredFields = new ArrayList<>(Arrays.stream((String[]) args[args.length - 1]).toList());

        String sql = SQLQueryUtil.removeParamsFromQuery(sqlWithParams, queryParameters);
        sql = sql.replace("WHERE", QueryGenerator.getReferencesJoin(references, tableName, ignoredFields) + " WHERE");

        logger.debug(Arrays.toString(queryParameters));
        logger.debug(sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (args != null) {
                for (int i = 0; i < queryParameters.length; i++) {
                    final int optionalIndex = i;
                    Optional<Parameter> optional = Arrays.stream(parameters)
                            .filter(parameter -> parameter.getAnnotation(Param.class).value().equals(queryParameters[optionalIndex]))
                            .findFirst();

                    if (optional.isEmpty()) {
                        throw new RuntimeException(queryParameters[i] + " not found");
                    }

                    int position = Arrays.stream(parameters).toList().indexOf(optional.get());
                    statement.setObject(i + 1, args[position]);
                }
            }

            result = statementListExecutor(statement, ignoredFields);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<Object> executeFindQuery(Method method, Object[] args) {
        String sql = QueryGenerator.generateQuery(method, tableName, valueClass, references, args);
        List<Object> result = new ArrayList<>();
        List<String> ignoredFields = new ArrayList<>(Arrays.stream((String[]) args[args.length - 1]).toList());

        Pageable pageable = (Pageable) Arrays.stream(args).filter(arg -> arg.getClass().isAssignableFrom(Pageable.class)).findFirst().orElse(null);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < args.length - 1; i++) {
                if (!args[i].getClass().equals(Pageable.class) && !ignoredFields.contains(args[i]))
                    statement.setObject(i + 1, args[i]);
            }
            result = statementListExecutor(statement, ignoredFields);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (pageable != null) {
            try {
                pageable.setTotalPages(
                        ((Long) executeCountQuery(repositoryInterface.getMethod("countAll"), null) - pageable.getOffset() - 1) / pageable.getLimit()
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    private Object saveEntity(Object entity) {
        if (entity == null)
            return entity;

        if (List.class.isAssignableFrom(entity.getClass())) {
            for (Object subEntity : ((List) entity)) {
                saveEntity(subEntity);
            }
            return entity;
        }

        String entityTableName = EntityUtils.getTableName(entity.getClass());
        String entityFields = EntityUtils.getColumnNameStream(entity.getClass())
                .collect(Collectors.joining(","));
        String sql = "INSERT INTO " + entityTableName + " (" + entityFields + ") VALUES (" + EntityUtils.generateQuestionMarks(entityFields) + ") RETURNING " + EntityUtils.getIdField(entity.getClass()).getName() + ";";


        try {
            Field idField = EntityUtils.getIdField(entity.getClass());
            idField.setAccessible(true);
            Object id = idField.get(entity);

            try {
                if (id != null) {
                    entityFields = EntityUtils.getColumnNameStream(entity.getClass())
                            .collect(Collectors.joining(" = ?,")) + " = ?";
                    sql = "UPDATE " + entityTableName + " SET " + entityFields + " WHERE " + EntityUtils.getColumnName(idField) + " = ?";
                }
            } catch (IllegalArgumentException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            logger.debug(sql);

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                int k = 1;
                Field[] fields = entity.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (EntityUtils.isNotPK(field) && EntityUtils.isNotFK(field)) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(entity);

                        if (!EntityUtils.isNotRelation(field)) {
                            Field fieldFK = EntityUtils.getIdField(field.getType());
                            fieldFK.setAccessible(true);
                            fieldValue = fieldFK.get(field.get(entity));
                        }

                        statement.setObject(k, fieldValue);
                        k++;
                    } else if (!EntityUtils.isNotFK(field) && !field.isAnnotationPresent(OneToMany.class)) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(entity);

                        Field idFKField = EntityUtils.getIdField(fieldValue.getClass());
                        idFKField.setAccessible(true);

                        Object idFK = idFKField.get(fieldValue);
                        statement.setObject(k, idFK);
                        k++;
                    }
                }
                if (id != null) {
                    statement.setObject(k, id);
                }
                statement.execute();

                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Object generatedKey = generatedKeys.getObject(1);
                    idField.set(entity, generatedKey);
                }
            }

        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return entity;
    }
}