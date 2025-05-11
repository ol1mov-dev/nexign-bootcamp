package com.projects.crm.controllers;

import com.projects.crm.controllers.requests.CreateUserRequest;
import com.projects.crm.controllers.responses.UserCreatedResponse;
import com.projects.crm.controllers.responses.UserInfoResponse;
import com.projects.crm.entities.User;
import com.projects.crm.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('canReadUserInfo')")
    @GetMapping("/{id}")
    public ResponseEntity<UserInfoResponse> getInfo(@PathVariable long id) {

        return userService.getInfo(id);
    }

    @PreAuthorize("hasAnyAuthority('canCreateUser')")
    @PostMapping("/create")
    public ResponseEntity<UserCreatedResponse> create(@RequestBody CreateUserRequest createUserRequest) {
        return userService.create(createUserRequest);
    }

}
