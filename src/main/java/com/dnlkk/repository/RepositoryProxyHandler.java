package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.repository.annotations.Param;
import com.dnlkk.repository.annotations.Query;
import com.dnlkk.repository.dnlkk_connection_pool.DnlkkDataSourceFactory;
import com.dnlkk.util.SQLQueryUtil;

import lombok.Data;

@Data
public class RepositoryProxyHandler implements InvocationHandler { 
    private final DataSource dataSource;
    private String tableName;
    private Class<?> keyClass;
    private Class<?> valueClass;
    private Class<?> repositoryInterface;

    private final Logger logger = LoggerFactory.getLogger(RepositoryProxyHandler.class);

    public RepositoryProxyHandler(Class<?> clazz) {
        repositoryInterface = clazz;
        this.dataSource = DnlkkDataSourceFactory.createDataSource();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {           
        List<Object> result = new ArrayList<>();  
        if (method.isAnnotationPresent(Query.class)) {
            result = execute(method.getAnnotation(Query.class).value(), args, method.getParameters());
        }
        else if (method.getName().startsWith(QueryOperation.FIND.getValue())) {
            result = executeFind(QueryGenerator.generateQuery(method, tableName, valueClass), args);
        } else if (method.getName().startsWith(QueryOperation.COUNT.getValue()) || method.getName().startsWith(QueryOperation.SUM.getValue())) {
            String sql = QueryGenerator.generateQuery(method, tableName, valueClass);
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
        logger.debug(Arrays.toString(queryParameters));
        logger.debug(sql);

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
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                // Создание объекта на основе данных из resultSet
                try {
                    Object entity = valueClass.getConstructor().newInstance();
                    Field[] fields = valueClass.getDeclaredFields();
                    for (Field field : fields) {
                        Object retrievedObject = resultSet.getObject(field.getName());
                        DependencyInjector.setField(entity, retrievedObject, field);
                    }
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
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        try {
                            Object entity = valueClass.getConstructor().newInstance();

                            Field[] fields = valueClass.getDeclaredFields();
                            for (Field field : fields) {
                                Object retrievedObject = resultSet.getObject(field.getName());
                                DependencyInjector.setField(entity, retrievedObject, field);
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
        String sql = "INSERT INTO " + tableName + " (name, surname, earnings) VALUES (?, ?, ?)";

        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            if (executeSelectByIdQuery(id) != null){
                sql = "UPDATE " + tableName + " SET name = ?, surname = ?, earnings = ? WHERE id = " + id;
            }
   
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int k = 1;
                    Field[] fields = entity.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getName() != "id"){
                            field.setAccessible(true); 
                            Object fieldValue = field.get(entity);
                            System.out.println(fieldValue);
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
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | SQLException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;   
    }
}