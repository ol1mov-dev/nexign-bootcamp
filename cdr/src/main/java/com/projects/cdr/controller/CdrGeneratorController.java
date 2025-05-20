package com.projects.cdr.controller;

import com.projects.cdr.service.CdrGeneratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cdr")
public class CdrGeneratorController {
    private final CdrGeneratorService cdrGeneratorService;

    public CdrGeneratorController(CdrGeneratorService cdrGeneratorService) {
        this.cdrGeneratorService = cdrGeneratorService;
    }

    @GetMapping("/generate")
    public void generate() throws InterruptedException {
        cdrGeneratorService.generate();
    }
}
