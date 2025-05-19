package com.gab.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // disable CSRF (safe for Postman/JSON-based APIs)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/signup", "/auth/login").permitAll() // allow only /auth/signup
                .anyRequest().authenticated()               // block everything else
            )
            .formLogin(form -> form.disable())             // disable login page
            .httpBasic(httpBasic -> httpBasic.disable())   // disable basic auth
            .build();
    }
}
