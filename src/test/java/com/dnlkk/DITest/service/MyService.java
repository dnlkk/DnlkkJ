package com.dnlkk.DITest.service;

import java.util.Arrays;
import java.util.List;

import com.dnlkk.DITest.dtos.UsersDTO;
import com.dnlkk.DITest.logic.Dummy;
import com.dnlkk.DITest.model.User;
import com.dnlkk.DITest.repository.TestRepository;
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
    private Dummy dummy;
    @AutoInject
    private TestRepository testRepository;

    public void doSome() {
        System.out.println(dummy);
    }    

    public String service() {
        return String.format("that's %s's service!", name);
    }

    public UsersDTO repo() {
        List<User> usersWithNameRuslan = testRepository.findByName("Ruslan");
        return new UsersDTO(usersWithNameRuslan, "Ruslan");
    }
}