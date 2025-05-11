package com.projects.hrs.controller;

import com.projects.hrs.controller.requests.CreateAbonentHrsRequest;
import com.projects.hrs.controller.requests.CreateAbonentRequest;
import com.projects.hrs.service.AbonentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/abonent")
@RequiredArgsConstructor
public class AbonentController {
    @Autowired
    private AbonentService abonentService;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody CreateAbonentHrsRequest createAbonentHrsRequest){
        log.info("Create Abonent " + createAbonentHrsRequest.tariffId());
        return abonentService.create(createAbonentHrsRequest);
    }
}
