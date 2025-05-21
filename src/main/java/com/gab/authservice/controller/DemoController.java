package com.gab.authservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/api/demo/hello_user")
    @PreAuthorize("hasRole('USER')")
    public String securedHello() {
        return "You accessed a USER endpoint!";
    }

    @GetMapping("/api/demo/hello_admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String securedAdmin() {
        return "You accessed a ADMIN endpoint!";
    }

    @GetMapping("/api/demo/hello_public")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String securedPublic() {
        return "You accessed a PUBLIC endpoint!";
    }
}
