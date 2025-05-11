package com.projects.brt.controllers;

import com.projects.brt.dto.AbonentDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.repositories.AbonentRepository;
import com.projects.brt.service.AbonentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/abonent")
public class AbonentController {

    private final AbonentService abonentService;

    public AbonentController(AbonentService abonentService) {
        this.abonentService = abonentService;
    }

    @PostMapping("/create")
    public ResponseEntity<Long> create(@RequestBody  AbonentDto abonentDto) {
       return abonentService.create(abonentDto);
    }
}
