package com.dnlkk.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.dnlkk.dependency_injector.DependencyInjector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Data;

@Data
public class RepositoryProxyHandler implements InvocationHandler { 
    private final DataSource dataSource;
    private String tableName;
    private Class<?> keyClass;
    private Class<?> valueClass;

    public RepositoryProxyHandler() {
        // Инициализация подключения к базе данных
        try {
            Class.forName("org.postgresql.Driver");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/dnlkk_db");
            config.setUsername("dnlkk");
            config.setPassword("dnlkkpass");
            config.setMaximumPoolSize(10); // Максимальное количество соединений в пуле
            this.dataSource = new HikariDataSource(config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found", e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {             
        if (method.getName().startsWith("find")) {
            List<Object> result = executeFind(QueryGenerator.generateQuery(method, tableName, valueClass), args);
            if (result.size() == 1)
                return result.get(0);
            return result;
        } else if (method.getName().equals("save")) {
            Object result = save(args[0]);
            return result;
        }
        
        return null;
    }

    public List<Object> executeFind(String sql, Object[] args) {
        List<Object> result = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (args != null)
                    for (int i = 0; i < args.length; i++)
                        statement.setObject(i + 1, args[i]);
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
                            result.add(entity);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    return result;
                }
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
                        // Создание объекта на основе данных из resultSet
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
        return null; // Запись с заданным id не найдена
    }
    
    public Object save(Object entity) {
        String sql = "INSERT INTO " + tableName + " (name, surname) VALUES (?, ?)"; // Замените на соответствующий SQL

        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            if (executeSelectByIdQuery(id) != null){
                sql = "UPDATE " + tableName + " SET name = ?, surname = ? WHERE id = " + id;
            }
   
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int k = 1;
                    Field[] fields = entity.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getName() != "id"){
                            field.setAccessible(true); // Установите доступность поля, если оно private
                            Object fieldValue = field.get(entity); // Получите значение поля из объекта entity
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