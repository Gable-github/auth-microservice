package com.gab.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gab.authservice.dto.LoginRequest;
import com.gab.authservice.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Enables Testcontainers support for this test class
@Testcontainers
// Loads the full Spring Boot application context
@SpringBootTest
// Sets up MockMvc for HTTP endpoint testing
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    // Spins up a real PostgreSQL container for the test
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb") // Name of the test database
            .withUsername("testuser")   // Username for the test DB
            .withPassword("testpass"); // Password for the test DB

    // Injects the container's DB connection details into Spring Boot, instead of using the actual postgres config in application.properties
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "test_jwt_secret_which_is_long_enough_123456");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private MockMvc mockMvc; // Used to simulate HTTP requests to the app

    @Autowired
    private ObjectMapper objectMapper; // Used to convert Java objects to JSON

    @Test
    void signup_and_login_shouldSucceed() throws Exception {
        // Simulate a user signup via HTTP POST
        SignupRequest signupRequest = new SignupRequest("integration@example.com", "password");
        // mockMvc.perform() executes a request and returns a ResultActions object for chaining expectations
        // post("/auth/signup") creates a POST request to the /auth/signup endpoint
        mockMvc.perform(post("/auth/signup")
                // contentType() sets the Content-Type header to application/json
                .contentType(MediaType.APPLICATION_JSON)
                // content() sets the request body by converting signupRequest to JSON string
                .content(objectMapper.writeValueAsString(signupRequest)))
                // andExpect() chains assertions about the response
                // status().isOk() verifies HTTP 200 status code
                .andExpect(status().isOk())
                // content().string() verifies the response body matches exactly
                .andExpect(content().string("User registered successfully"));

        // Simulate a user login via HTTP POST
        LoginRequest loginRequest = new LoginRequest("integration@example.com", "password");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String token = result.getResponse().getContentAsString();
                    assertNotNull(token); // Should not be null
                    // JWTs have three parts separated by periods
                    String[] parts = token.split("\\.");
                    assertEquals(3, parts.length, "Response is not a valid JWT token");
                });
    }
}