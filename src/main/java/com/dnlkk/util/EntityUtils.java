package com.dnlkk.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.dnlkk.repository.annotations.entity.Column;
import com.dnlkk.repository.annotations.entity.FK;
import com.dnlkk.repository.annotations.entity.PK;
import com.dnlkk.repository.annotations.entity.Table;
import com.dnlkk.repository.annotations.entity.ManyToMany;
import com.dnlkk.repository.annotations.entity.ManyToOne;
import com.dnlkk.repository.annotations.entity.OneToMany;
import com.dnlkk.repository.annotations.entity.OneToOne;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getTableName(Class<?> clazz) {
        return clazz.getAnnotation(Table.class).value();
    }

    public static String getRelationIdField(Class<?> clazz) {
        Optional<Field> optIdField = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)).findFirst();
        if (optIdField.isEmpty())
            throw new RuntimeException("@Id must be in entity");
        if ( optIdField.get().isAnnotationPresent(ManyToOne.class))
            return optIdField.get().getAnnotation(ManyToOne.class).value();
        return optIdField.get().getAnnotation(OneToOne.class).value();
    }

    public static String getRelationIdFieldName(Class<?> clazz) {
        Optional<Field> optIdField = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(PK.class)).findFirst();
        if (optIdField.isEmpty())
            throw new RuntimeException("@Id must be in entity");
        return getColumnName(optIdField.get());
    }    

    public static Field getIdField(Class<?> clazz) {
        Optional<Field> optIdField = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(PK.class)).findFirst();
        if (optIdField.isEmpty())
            throw new RuntimeException("@Id must be in entity");
        return optIdField.get();
    }

    public static String getColumnName(Field columnField) {
        if (columnField.isAnnotationPresent(Column.class)){
            return columnField.getAnnotation(Column.class).value();
        }
        return columnField.getName();
    }

    public static Stream<String> getColumnNameStream(Class<?> valueClass) {
        return (Stream<String>) Arrays.stream(valueClass.getDeclaredFields())
            .filter(field -> isNotPK(field) && isNotFK(field))
            .map(EntityUtils::getColumnName);
    }

    public static boolean isNotPK(Field field) {
        return !field.isAnnotationPresent(PK.class);
    }

    public static boolean isNotFK(Field field) {
        return !field.isAnnotationPresent(FK.class);
    }

    public static boolean isNotRelation(Field field) {
        return !field.isAnnotationPresent(OneToMany.class) && !field.isAnnotationPresent(ManyToMany.class) 
                && !field.isAnnotationPresent(ManyToOne.class)  && !field.isAnnotationPresent(OneToOne.class);
    }

    public static String generateQuestionMarks(String input) {
        String[] parts = input.split(",");
        StringBuilder result = new StringBuilder();
    
        for (int i = 0; i < parts.length; i++) {
            result.append("?");
            if (i < parts.length - 1) {
                result.append(",");
            }
        }
    
        return result.toString();
    }
}