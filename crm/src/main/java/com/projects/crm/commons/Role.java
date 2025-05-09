package com.projects.crm.commons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER(
            Set.of(
                    Permission.CAN_TOP_UP_BALANCE,
                    Permission.CAN_READ_USER_INFO
            )
    ),

    MANAGER(Set.of(Permission.values()));

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getGrantedAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission
                        -> new SimpleGrantedAuthority(permission.getPermission())
                )
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}