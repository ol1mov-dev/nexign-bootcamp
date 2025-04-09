package com.projects.cdr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RequestMapping("/api/v1/cdr")
@RestController
public class CdrController {
    public void a(){
        System.out.println(123);
    }
}
