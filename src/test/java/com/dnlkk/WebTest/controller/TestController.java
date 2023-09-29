package com.dnlkk.WebTest.controller;

import com.dnlkk.WebTest.dtos.RequestParamTestDTO;
import com.dnlkk.WebTest.dtos.UserDTO;
import com.dnlkk.WebTest.dtos.UserResponseDTO;
import com.dnlkk.WebTest.dtos.UsersDTO;
import com.dnlkk.WebTest.service.MyService;
import com.dnlkk.controller.annotations.*;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.components.RestController;

import java.util.Arrays;

@RestController("/test")
public class TestController {
    @AutoInject
    public MyService myService;

    @Get
    @RequestMapping("/")
    public ResponseEntity<String> getStart() {
        return ResponseEntity.ok("ping");
    }

    @Get
    @RequestMapping("/test")
    public String getTest() {
        return "test testtt!!! ТЕСТОООО";
    }

    @Post
    @RequestMapping("/test")
    public ResponseEntity<UserResponseDTO> getTestUser(@RequestBody UserDTO userDto) {
        return ResponseEntity.ok(new UserResponseDTO(userDto));
    }

    @Get
    @RequestMapping("/test/:id")
    public ResponseEntity<String> getTestUserPath(@PathVar("id") Integer id) {
        return ResponseEntity.ok(String.format("Path var is %d", id));
    }

    @Get
    @RequestMapping("/test2")
    public ResponseEntity<RequestParamTestDTO> getTest2(@RequestParam("err") Integer[] err, @RequestParam("code") Integer code, @RequestParam("name") String name) {
        return ResponseEntity.ok(new RequestParamTestDTO(err, code, name));
    }

    @Get
    @RequestMapping("/service")
    public String getService() {
        return myService.service();
    }

    @Get
    @RequestMapping("/repo")
    public ResponseEntity<UsersDTO> getRepo() {
        return ResponseEntity.ok(myService.repo());
    }
}