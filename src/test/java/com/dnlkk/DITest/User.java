package com.dnlkk.DITest;

import java.math.BigDecimal;

import com.dnlkk.dependency_injector.annotations.components.Table;

import lombok.Data;

@Table("user_table")
@Data
public class User {
    private Integer id;
    private String name;
    private String surname;
    private BigDecimal earnings;
}