package com.dnlkk.WebTest.controller;

import com.dnlkk.WebTest.dtos.UsersDTO;
import com.dnlkk.WebTest.service.MyService;
import com.dnlkk.controller.annotations.Get;
import com.dnlkk.controller.annotations.RequestParam;
import com.dnlkk.controller.annotations.RequestMapping;
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

    @Get
    @RequestMapping("/test2")
    public String getTest2(@RequestParam("err") Integer[] err, @RequestParam("code") Integer code, @RequestParam("name") String name) {
        return Arrays.toString(err) + " " + code + " " + name;
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