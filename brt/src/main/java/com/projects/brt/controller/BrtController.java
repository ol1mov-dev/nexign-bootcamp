package com.projects.brt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/brt")
@RestController
public class BrtController {

    @GetMapping("/test")
    public String brt() {
        return "Hello World";
    }
}
