package com.projects.cdr.controller;

import com.projects.cdr.service.CdrService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cdr")
@AllArgsConstructor
public class CdrController {
    private final CdrService cdrService;

    @GetMapping("/generate")
    public void generate() throws InterruptedException {
        cdrService.generate();
    }
}
