package com.dnlkk.DITest;

import com.dnlkk.dependency_injector.annotations.components.Table;

import lombok.Data;

@Table(tableName = "user_table")
@Data
public class User {
    private Integer id;
    private String name;
    private String surname;
}