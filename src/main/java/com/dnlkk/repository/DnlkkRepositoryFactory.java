package com.dnlkk.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.dnlkk.repository.annotations.entity.With;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dnlkk.repository.annotations.entity.Table;
import com.dnlkk.util.EntityUtils;

import lombok.Data;

@Data
public class DnlkkRepositoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(DnlkkRepositoryFactory.class);

    public static <K, V> DnlkkRepository<K, V> createRepositoryInstance(Class<?> clazz) {
        if (DnlkkRepository.class.isAssignableFrom(clazz)) {
            RepositoryProxyHandler handler = new RepositoryProxyHandler(clazz);
            String tableName = extractTableNameFromEntityClass(getValueClass(clazz));
            handler.setTableName(tableName);
            handler.setKeyClass(getKeyClass(clazz));
            handler.setValueClass(getValueClass(clazz));
            handler.setReferences(Arrays.stream(handler.getValueClass().getDeclaredFields())
                    .filter(field -> !EntityUtils.isNotRelation(field) || field.isAnnotationPresent(With.class))
                    .toList());

            try {
                DnlkkRepository proxyObject = (DnlkkRepository) Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        new Class[]{clazz, DnlkkRepository.class},
                        handler
                );
                if (proxyObject == null) {
                    logger.error("Repository creation failed");
                    throw new Exception("Repository creation failed");
                } else
                    return proxyObject;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        logger.error("Not a repository");
        throw new IllegalArgumentException("Not a repository.");
    }

    private static <K, V> String extractTableNameFromEntityClass(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);

        if (tableAnnotation != null)
            return tableAnnotation.value();

        logger.error("No @Table annotation found on the entity class");
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
        logger.error("Unable to determine the key type");
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
        logger.error("Unable to determine the value type");
        throw new IllegalArgumentException("Unable to determine the value type.");
    }
}