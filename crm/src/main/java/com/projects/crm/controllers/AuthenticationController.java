package com.projects.crm.controllers;

import com.projects.crm.controllers.requests.AuthenticateUserRequest;
import com.projects.crm.controllers.requests.RegisterUserRequest;
import com.projects.crm.controllers.responses.JwtResponse;
import com.projects.crm.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    @Autowired
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody RegisterUserRequest request
    ) { return authenticationService.register(request); }

    @PostMapping("/authenticate")
    public ResponseEntity<JwtResponse> authenticate(
            @Valid @RequestBody AuthenticateUserRequest request
    ){
        log.info("Authenticating user: {}", request.toString());
        return authenticationService.authenticate(request);
    }
}
