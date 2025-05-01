package com.projects.hrs.controller;

import com.projects.hrs.controller.requests.CreateClientRequest;
import com.projects.hrs.entities.Client;
import com.projects.hrs.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/create")
    public ResponseEntity<Client> create(@RequestBody CreateClientRequest request) {
        return ResponseEntity.ok(clientService.create(request));
    }

}
