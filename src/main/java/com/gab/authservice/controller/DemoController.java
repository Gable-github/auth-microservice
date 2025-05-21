package com.gab.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/api/demo/hello")
    public String securedHello() {
        return "You accessed a secured endpoint!";
    }
}
