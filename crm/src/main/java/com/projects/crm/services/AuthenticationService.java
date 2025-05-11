package com.projects.crm.services;

import com.projects.crm.commons.Role;
import com.projects.crm.controllers.requests.AuthenticateUserRequest;
import com.projects.crm.controllers.requests.RegisterUserRequest;
import com.projects.crm.controllers.responses.JwtResponse;
import com.projects.crm.entities.User;
import com.projects.crm.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ){
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<JwtResponse> register(@Valid RegisterUserRequest request) {
        User savedUser = userRepository.save(
                    User
                        .builder()
                        .firstName(request.firstname())
                        .name(request.name())
                        .lastName(request.lastname())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .role(Role.USER)
                        .build()
        );


        String jwtToken = jwtService.generateToken(
                request.email(),
                Map.of()
        );
        return ResponseEntity.ok(
                JwtResponse.builder().token(jwtToken).build()
        );
    }

    public ResponseEntity<JwtResponse> authenticate(AuthenticateUserRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        if (authentication.isAuthenticated()) {
            log.info("Authentication successful");
            return ResponseEntity.ok(
                    JwtResponse.builder()
                            .token(jwtService.generateToken(request.email(), Map.of()))
                            .build()
            );
        }

        log.info("Authentication failed");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        JwtResponse
                                .builder()
                                .token(null)
                                .build()
                );
    }
}