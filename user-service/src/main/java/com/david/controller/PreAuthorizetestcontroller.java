package com.david.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/preauthorize/test")
public class PreAuthorizetestcontroller {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') and hasAuthority('test') and hasAuthority('read')")
    public String test(Authentication authentication) {
        return "preauthorize test success! User: " + authentication.getName() +
                ", Authorities: " + authentication.getAuthorities();
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminOnly(Authentication authentication) {
        return "Admin only content! User: " + authentication.getName();
    }

    @GetMapping("/test-authority")
    @PreAuthorize("hasAuthority('test')")
    public String testAuthority(Authentication authentication) {
        return "Test authority content! User: " + authentication.getName();
    }
}
