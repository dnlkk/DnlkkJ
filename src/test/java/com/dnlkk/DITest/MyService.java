package com.dnlkk.DITest;

import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.components.Service;

import lombok.Data;

@Service
@Data
public class MyService {
    private String name = "anton!";
    @AutoInject
    private Dummy dummy;

    public void doSome() {
        System.out.println(dummy);
    }
}