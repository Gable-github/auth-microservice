package com.gab.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gab.authservice.config.TestConfig;
import com.gab.authservice.dto.LoginRequest;
import com.gab.authservice.dto.SignupRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

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
@Import(TestConfig.class)
@ActiveProfiles("local")
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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("aws.secrets.enabled", () -> "false");
    }

    @BeforeAll
    static void setup() throws NoSuchAlgorithmException, IOException {
        // Create test keys directory
        Path testKeysDir = Paths.get("src/test/resources/keys");
        Files.createDirectories(testKeysDir);

        // Generate RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Save private key
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        Files.write(testKeysDir.resolve("private.pem"), privateKeyPEM.getBytes());

        // Save public key
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        Files.write(testKeysDir.resolve("public.pem"), publicKeyPEM.getBytes());
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