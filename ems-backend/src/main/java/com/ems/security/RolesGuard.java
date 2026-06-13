package com.ems.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("rolesGuard")
public class RolesGuard {

    public boolean hasRole(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> Arrays.stream(roles)
                        .anyMatch(role -> auth.equals("ROLE_" + role)));
    }
}
