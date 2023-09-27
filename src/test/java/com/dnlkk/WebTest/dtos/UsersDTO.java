package com.dnlkk.WebTest.dtos;

import java.util.List;

import com.dnlkk.WebTest.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersDTO {
    private List<User> users;
    private String name;
}