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
import java.util.List;

import com.dnlkk.dependency_injector.DependencyInjector;

import lombok.Data;

@Data
public class RepositoryProxyHandler implements InvocationHandler { 
    private final Connection connection;
    private String tableName;
    private Class<?> keyClass;
    private Class<?> valueClass;

    public RepositoryProxyHandler() {
        // Инициализация подключения к базе данных
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/dnlkk_db", "dnlkk", "dnlkkpass");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish a database connection", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found", e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Обработка вызовов методов и выполнение операций с базой данных
        if (method.getName().equals("findAll")) {
            // Пример: выполнение SELECT * FROM table_name
            // и возврат результата как список объектов
            List<Object> result = executeSelectAllQuery();
            return result;
        } else if (method.getName().equals("findById")) {
            // Пример: выполнение SELECT * FROM table_name WHERE id = ?
            // и возврат результата как объекта
            Object result = executeSelectByIdQuery(args[0]);
            return result;
        }
        // Другие методы могут быть обработаны аналогичным образом
        return null;
    }

    public List<Object> executeSelectAllQuery() throws SQLException {
        List<Object> result = new ArrayList<>();
        
            // SQL-запрос на выбор всех записей из таблицы tableName
            String sql = "SELECT * FROM " + tableName;
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Object> resultList = new ArrayList<>();
                    while (resultSet.next()) {
                        // Создание объекта на основе данных из resultSet
                        try {
                            Object entity = valueClass.getConstructor().newInstance();

                            Field[] fields = valueClass.getDeclaredFields();
                            for (Field field : fields) {
                                Object retrievedObject = resultSet.getObject(field.getName());
                                DependencyInjector.setField(entity, retrievedObject, field);
                            }
                            resultList.add(entity);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                     return resultList;
                }
            }
    }
    
    public Object executeSelectByIdQuery(Object id) throws SQLException {
            // SQL-запрос на выбор записи по идентификатору
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            
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
        
        return null; // Запись с заданным id не найдена
    }
    
    public Object save(Object entity) throws SQLException {
            // SQL-запрос на сохранение (INSERT) или обновление (UPDATE) сущности
            String sql = "INSERT INTO " + tableName + " (...) VALUES (...)"; // Замените на соответствующий SQL
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Установите параметры для SQL-запроса на основе значений сущности entity
                // statement.setXXX(index, value);
                
                // Выполните запрос
                statement.executeUpdate();
                
                // Верните результат сохранения (например, сгенерированный ключ)
                return entity; // Замените на фактический результат
            }
        }
    
}