package com.dnlkk;

import com.dnlkk.dependency_injector.annotations.AutoInject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class TestComponent {
    private final String text;
    @AutoInject
    private Dummy dummy;
}