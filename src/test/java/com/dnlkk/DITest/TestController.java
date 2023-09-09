package com.dnlkk.DITest;

import com.dnlkk.dependency_injector.annotations.components.RestController;

import lombok.Data;

@RestController(path = "/test")
@Data
public class TestController {
}