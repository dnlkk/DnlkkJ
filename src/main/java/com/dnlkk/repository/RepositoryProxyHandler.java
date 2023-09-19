package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.tokens.Token.ID;

import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.repository.annotations.Param;
import com.dnlkk.repository.annotations.Query;
import com.dnlkk.repository.annotations.entity.Column;
import com.dnlkk.repository.annotations.entity.Id;
import com.dnlkk.repository.annotations.entity.ManyToMany;
import com.dnlkk.repository.annotations.entity.ManyToOne;
import com.dnlkk.repository.annotations.entity.OneToMany;
import com.dnlkk.repository.annotations.entity.OneToOne;
import com.dnlkk.repository.annotations.entity.Table;
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {           
        List<Object> result = new ArrayList<>();  
        if (method.isAnnotationPresent(Query.class)) {
            result = execute(method.getAnnotation(Query.class).value(), args, method.getParameters());
        }
        else if (method.getName().startsWith(QueryOperation.FIND.getValue())) {
            result = executeFind(QueryGenerator.generateQuery(method, tableName, valueClass, references), args);
        } else if (method.getName().startsWith(QueryOperation.COUNT.getValue()) || method.getName().startsWith(QueryOperation.SUM.getValue())) {
            String sql = QueryGenerator.generateQuery(method, tableName, valueClass, references);
            return executeCount(sql, args);
        } else if (method.getName().equals(QueryOperation.SAVE.getValue())) {
            return save(args[0]);
        }
        if (method.getReturnType() != List.class) {
            if (!result.isEmpty())
                return result.get(0);
            else
                return null;
        }
        return method.getReturnType().cast(result);
    }


    public Object executeCount(String sql, Object[] args) throws SQLException {
        Object result = 0;
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (args != null)
                    for (int i = 0; i < args.length; i++)
                        statement.setObject(i + 1, args[i]);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next())
                        result = resultSet.getObject(1);
                }
            }
        }
        return result;
    }

    public List<Object> execute(String sqlWithParams, Object[] args, Parameter[] parameters) {
        List<Object> result = new ArrayList<>();

        String[] queryParameters = SQLQueryUtil.getParamsFromQuery(sqlWithParams);
        String sql = SQLQueryUtil.removeParamsFromQuery(sqlWithParams, queryParameters);
        sql = sql.replace("WHERE", QueryGenerator.getReferencesJoin(references, tableName) + " WHERE");
        logger.debug(Arrays.toString(queryParameters));
        logger.debug(sql);

        Object id;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (args != null)
                    for (int i = 0; i < queryParameters.length; i++) {
                        final int optionalIndex = i;
                        Optional<Parameter> optional = Arrays.stream(parameters).filter(parameter -> parameter.getAnnotation(Param.class).value().equals(queryParameters[optionalIndex])).findFirst();

                        if (optional.isEmpty()) 
                            throw new RuntimeException(queryParameters[i] + " not found");
                        int position = Arrays.stream(parameters).toList().indexOf(optional.get());
                        statement.setObject(i + 1, args[position]);
                    }
                result = statementListExecutor(statement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Object> statementListExecutor(PreparedStatement statement) throws SQLException {
        List<Object> resultFunction = new ArrayList<>();
        Object id = null; // TODO: id check
        boolean idChange = false;

        Map<String, Object> oneToMany = new HashMap<>();
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
                        if (!EntityUtils.isNotId(field)) {
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
                                    if (!EntityUtils.isNotRelation(field2) && oneToMany.get(String.format("%s%s", id.toString(), field2.getName())) == null && !field2.isAnnotationPresent(OneToOne.class))
                                            oneToMany.put(String.format("%s%s", id.toString(), field2.getName()), new ArrayList<>());
                                }
                            } else {
                                idChange = false;
                            }
                        }

                        if (EntityUtils.isNotRelation(field)){
                            Object retrievedObject = resultSet.getObject(EntityUtils.getColumnName(field));
                            DependencyInjector.setField(entity, retrievedObject, field);
                        }
                        else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToOne.class)) {
                            Class<?> relationClazz = null;
                            if (field.isAnnotationPresent(OneToOne.class))
                                relationClazz = field.getType();
                            else
                                relationClazz = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            
                            Object relationEntity = relationClazz.getDeclaredConstructor().newInstance();
                            Object relationFromId = null;

                            for (Field relationField : relationClazz.getDeclaredFields()) {
                                if (EntityUtils.isNotRelation(relationField)) {
                                    Object retrievedObject = resultSet.getObject(EntityUtils.getColumnName(relationField));
                                    
                                    DependencyInjector.setField(relationEntity, retrievedObject, relationField);
                                } else if (relationField.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToOne.class)) {
                                    relationFromId = resultSet.getObject(EntityUtils.getColumnName(relationField));
                                    
                                    if (relationFromId != null) { 
                                        Object relationEntityManyToOne = null;
                                        if (entityToIdMap.containsKey(relationFromId)) {
                                            if (field.isAnnotationPresent(ManyToMany.class))
                                                 ((List<Object>) entityToIdMap.get(relationFromId)).add(relationEntityManyToOne);
                                            else
                                                relationEntityManyToOne = entityToIdMap.get(relationFromId);
                                        } else {
                                            if (field.isAnnotationPresent(ManyToMany.class)) {
                                                entityToIdMap.put(relationFromId, new ArrayList<>());
                                                ((List<Object>) entityToIdMap.get(relationFromId)).add(relationEntityManyToOne);
                                            }
                                            else{
                                                relationEntityManyToOne = valueClass.getConstructor().newInstance();
                                                entityToIdMap.put(relationFromId, relationEntityManyToOne);
                                            }
                                        }
                                        DependencyInjector.setField(relationEntity, relationEntityManyToOne, relationField);
                                        
                                        String relationKey = String.format("%s%s", relationFromId.toString(), field.getName());

                                        
                                        if (field.isAnnotationPresent(OneToOne.class)) {
                                            if (oneToMany.get(relationKey) == null)
                                                oneToMany.put(relationKey, relationEntity);
                                        }
                                        else {
                                            if (oneToMany.get(relationKey) == null)
                                                oneToMany.put(relationKey, new ArrayList<>());
                                            if (((relationField.isAnnotationPresent(ManyToOne.class) && relationField.getAnnotation(ManyToOne.class).value().equals(field.getName()))
                                            || (relationField.isAnnotationPresent(ManyToMany.class) && relationField.getAnnotation(ManyToMany.class).value().equals(field.getName()))) 
                                            && !((List<Object>) oneToMany.get(relationKey)).contains(relationEntity))
                                                ((List<Object>) oneToMany.get(relationKey)).add(relationEntity);
                                        }
                                    }
                                }
                            }

                            Object list = oneToMany.get(String.format("%s%s", id.toString(), field.getName()));

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

    public List<Object> executeFind(String sql, Object[] args) {
        List<Object> result = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (args != null)
                    for (int i = 0; i < args.length; i++)
                        statement.setObject(i + 1, args[i]);
                result = statementListExecutor(statement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public Object executeSelectByIdQuery(Object id) throws SQLException {
        Field idField = EntityUtils.getIdField(valueClass);
        String sql = "SELECT * FROM " + tableName + " WHERE " + EntityUtils.getColumnName(idField) + " = ?";
            
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        try {
                            Object entity = valueClass.getConstructor().newInstance();

                            Field[] fields = valueClass.getDeclaredFields();
                            for (Field field : fields) {
                                if (EntityUtils.isNotRelation(field)){
                                    Object retrievedObject = resultSet.getObject(field.getName());
                                    DependencyInjector.setField(entity, retrievedObject, field);
                                }
                            }
                            return entity;
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Object save(Object entity) {
        String entityFields = EntityUtils.getColumnNameStream(valueClass)
            .collect(Collectors.joining(","));
        String sql = "INSERT INTO " + tableName + " (" + entityFields + ") VALUES (" + EntityUtils.generateQuestionMarks(entityFields) + ")";

        try {
            Field idField = EntityUtils.getIdField(valueClass);
            idField.setAccessible(true);
            Object id = idField.get(entity);
            if (executeSelectByIdQuery(id) != null){
                entityFields = EntityUtils.getColumnNameStream(valueClass)
            .collect(Collectors.joining(" = ?,")) + " = ?";
                sql = "UPDATE " + tableName + " SET " + entityFields + " WHERE " + EntityUtils.getColumnName(idField) + " = " + id;
            }
   
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int k = 1;
                    Field[] fields = entity.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if (EntityUtils.isNotId(field) && EntityUtils.isNotRelation(field)){
                            field.setAccessible(true); 
                            Object fieldValue = field.get(entity);
                            statement.setObject(k, fieldValue);
                            k++;
                        }
                    }
                    statement.executeUpdate();
                    return entity;
                }
            } catch (SQLException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (SecurityException | IllegalArgumentException | SQLException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;   
    }
}