package com.gab.authservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public SecretsManagerClient secretsManagerClient() {
        // Create a mock AWS Secrets Manager client for testing
        SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
        
        // Mock the response for JWT keys (using test keys)
        GetSecretValueResponse mockResponse = GetSecretValueResponse.builder()
                .secretString("-----BEGIN PRIVATE KEY-----\nMOCK_TEST_KEY\n-----END PRIVATE KEY-----")
                .build();
        
        when(mockClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(mockResponse);
        
        return mockClient;
    }
} 