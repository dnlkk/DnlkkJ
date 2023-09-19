package com.dnlkk.DITest.dtos;

import java.util.List;

import com.dnlkk.DITest.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsersDTO {
    private List<User> users;
    private String name;
}