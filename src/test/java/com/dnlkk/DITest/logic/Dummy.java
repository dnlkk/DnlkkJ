package com.dnlkk.DITest.logic;

import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.components.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Dummy {
    private final String text;
    @AutoInject
    private Bobby bobby;

    public Dummy() {
        this.text = "silly!";
    }    
}