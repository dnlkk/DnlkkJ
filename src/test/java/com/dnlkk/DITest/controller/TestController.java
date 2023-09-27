package com.dnlkk.DITest.controller;

import com.dnlkk.DITest.dtos.UsersDTO;
import com.dnlkk.DITest.service.MyService;
import com.dnlkk.controller.annotations.Get;
import com.dnlkk.controller.annotations.RequestMapping;
import com.dnlkk.controller.responses.ResponseEntity;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.components.RestController;

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

//    @Get
//    @RequestMapping("/service")
//    public String getService() {
//        return myService.service();
//    }
//
//    @Get
//    @RequestMapping("/repo")
//    public ResponseEntity<UsersDTO> getRepo() {
//        return ResponseEntity.ok(myService.repo());
//    }
}