package com.gab.authservice.service;

import com.gab.authservice.dto.LoginRequest;
import com.gab.authservice.dto.SignupRequest;
import com.gab.authservice.entity.Role;
import com.gab.authservice.entity.User;
import com.gab.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void signup_shouldSaveUser() {
        // Create a new signup request with test email and password
        SignupRequest request = new SignupRequest("test@example.com", "password");

        // Mock userRepository to return false when checking if email exists
        // This simulates that the email is not already taken
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // Mock the password encoder to return "hashed" when encoding the password
        // This simulates password encryption
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");

        // Call the actual signup method being tested
        authService.signup(request);

        // Verify that userRepository.save() was called exactly once
        // with any User object as the parameter
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_shouldThrowExceptionIfEmailExists() {
        // Create a signup request with test email and password
        SignupRequest request = new SignupRequest("test@example.com", "password");

        // Mock userRepository to return true when checking if email exists
        // This simulates that the email is already taken
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Assert that calling signup with an existing email throws RuntimeException
        assertThrows(RuntimeException.class, () -> authService.signup(request));

        // Verify that userRepository.save() was never called
        // since the email already exists and an exception was thrown
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnToken() {
        String email = "test@example.com";
        String password = "password";
        String hashed = "hashed";
        String token = "jwt_token";
        User user = new User(UUID.randomUUID(), email, hashed, Role.USER);

        // Mock userRepository.findByEmail() to return the test user
        // We need Optional.of() because findByEmail() returns Optional<User>, not User directly
        // Even though we control the mock's return value, the return type must match the real method
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // The when().thenReturn() pattern:
        // 1. Ensures type safety - thenReturn value must match matches() return type (boolean)
        // 2. Sets up the mock behavior - when matches(password, hashed) is called, return true
        // The matches() call inside when() isn't actually executed - it's just used by Mockito 
        // to capture the method and arguments to mock
        when(passwordEncoder.matches(password, hashed)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(token);

        // When authService.login() is called, it will use the mocked jwtService
        // The mock was set up above with: when(jwtService.generateToken(user)).thenReturn(token)
        // So even though we're calling the real login method, it uses our mocked token generation
        String result = authService.login(new LoginRequest(email, password));
        assertEquals(token, result);
    }

    @Test
    void login_shouldThrowExceptionIfUserNotFound() {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(new LoginRequest(email, "password")));
    }

    @Test
    void login_shouldThrowExceptionIfPasswordInvalid() {
        String email = "test@example.com";
        String password = "wrongpassword";
        String hashed = "hashed";
        User user = new User(UUID.randomUUID(), email, hashed, Role.USER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashed)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(new LoginRequest(email, password)));
    }
}