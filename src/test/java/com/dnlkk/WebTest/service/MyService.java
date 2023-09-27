package com.dnlkk.WebTest.service;

import java.util.List;

import com.dnlkk.WebTest.dtos.UsersDTO;
import com.dnlkk.WebTest.model.User;
import com.dnlkk.WebTest.repository.TestRepository;
import com.dnlkk.boot.annotations.ConfigValue;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.components.Service;

import lombok.Data;

@Service
@Data
public class MyService {
    @ConfigValue("ruslan")
    private String name = "anton!";
    @AutoInject
    private TestRepository testRepository;

    public String service() {
        return String.format("that's %s's service!", name);
    }

    public UsersDTO repo() {
        List<User> usersWithNameRuslan = testRepository.findByName("Ruslan");
        return new UsersDTO(usersWithNameRuslan, "Ruslan");
    }
}