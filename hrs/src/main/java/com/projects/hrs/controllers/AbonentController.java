package com.projects.hrs.controllers;

import com.projects.hrs.controllers.requests.CreateAbonentHrsRequest;
import com.projects.hrs.services.AbonentService;
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
    public ResponseEntity<String> create(@RequestBody CreateAbonentHrsRequest createAbonentHrsRequest){
        return abonentService.create(createAbonentHrsRequest);
    }
}
