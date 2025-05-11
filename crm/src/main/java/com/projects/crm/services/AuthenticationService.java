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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<JwtResponse> register(@Valid RegisterUserRequest request) {
        userRepository.save(
                User
                        .builder()
                        .firstName(request.firstname())
                        .name(request.name())
                        .lastName(request.lastname())
                        .email(request.email())
                        .password(
                                passwordEncoder.encode(request.password())
                        )
                        .role(Role.USER)
                        .build()
        );

        String jwtToken = jwtService.generateToken(request.email());
        return ResponseEntity.ok(
                JwtResponse.builder().token(jwtToken).build()
        );
    }

    public ResponseEntity<JwtResponse> authenticate(AuthenticateUserRequest request) {
        log.info("123");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        log.info("authenticated user: {}", authentication.getPrincipal());
        if (authentication.isAuthenticated()) {
            log.info("Authentication successful");
            return ResponseEntity.ok(
                    JwtResponse.builder()
                            .token(jwtService.generateToken(request.email()))
                            .build()
            );
        }
        log.info("Authentication failed");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        JwtResponse.builder().token(null).build()
                );
    }
}