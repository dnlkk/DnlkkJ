package com.dnlkk.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import com.dnlkk.dependency_injector.annotations.components.Table;

import lombok.Data;

@Data
public class DnlkkRepositoryFactory {
    public static <K, V> DnlkkRepository createRepositoryInstance(Class<?> clazz) {
        if (DnlkkRepository.class.isAssignableFrom(clazz)) {
            RepositoryProxyHandler handler = new RepositoryProxyHandler();
            String tableName = extractTableNameFromEntityClass(getValueClass(clazz));
            handler.setTableName(tableName);
            handler.setKeyClass(getKeyClass(clazz));
            handler.setValueClass(getValueClass(clazz));
            
            try {
                DnlkkRepository proxyObject = (DnlkkRepository) Proxy.newProxyInstance( clazz.getClassLoader(), new Class[]{clazz, DnlkkRepository.class}, handler);
                if (proxyObject == null)
                    throw new Exception("Repository creation failed");
                else 
                    return proxyObject; 
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        throw new IllegalArgumentException("Not a repository.");
    }

    private static <K, V> String extractTableNameFromEntityClass(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
    
        if (tableAnnotation != null) 
            return tableAnnotation.value();
    
        throw new IllegalArgumentException("No @Table annotation found on the entity class.");
    }

    private static <K, V> Class<K> getKeyClass(Class<?> repositoryInterface) {
        ParameterizedType genericType = (ParameterizedType) repositoryInterface.getGenericInterfaces()[0];
        Type[] typeArguments = genericType.getActualTypeArguments();
        if (typeArguments.length >= 2) {
            @SuppressWarnings("unchecked")
            Class<K> keyClass = (Class<K>) typeArguments[0];
            return keyClass;
        }
        throw new IllegalArgumentException("Unable to determine the key type.");
    }

    public static <K, V> Class<V> getValueClass(Class<?> repositoryInterface) {
        ParameterizedType genericType = (ParameterizedType) repositoryInterface.getGenericInterfaces()[0];
        Type[] typeArguments = genericType.getActualTypeArguments();
        if (typeArguments.length >= 2) {
            @SuppressWarnings("unchecked")
            Class<V> valueClass = (Class<V>) typeArguments[1];
            return valueClass;
        }
        throw new IllegalArgumentException("Unable to determine the value type.");
    }
}