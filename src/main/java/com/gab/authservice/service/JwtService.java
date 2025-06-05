package com.gab.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.gab.authservice.entity.User;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final Environment environment;
    
    // Local development configuration
    @Value("${jwt.private-key-path}")
    private String privateKeyPath;
    
    @Value("${jwt.public-key-path}")
    private String publicKeyPath;
    
    // AWS configuration
    private final String secretName = "auth-microservice/jwt/keys";
    private final Region region = Region.of("us-east-1");
    private SecretsManagerClient client;

    private boolean isLocalProfile() {
        return environment.matchesProfiles("local");
    }

    private SecretsManagerClient getSecretsManagerClient() {
        if (client == null && !isLocalProfile()) {
            client = SecretsManagerClient.builder()
                    .region(region)
                    .build();
        }
        return client;
    }

    private String getSecretFromAWS() {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        try {
            GetSecretValueResponse getSecretValueResponse = getSecretsManagerClient().getSecretValue(getSecretValueRequest);
            return getSecretValueResponse.secretString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch JWT keys from AWS Secrets Manager", e);
        }
    }

    // Helper to parse AWS secret JSON and extract keys
    private Map<String, String> getAwsKeyMap() {
        String secretJson = getSecretFromAWS();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(secretJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse keys from AWS secret", e);
        }
    }

    private RSAPrivateKey getPrivateKey() {
        try {
            String keyContent;
            if (isLocalProfile()) {
                // Local development: Read from classpath
                keyContent = new String(new ClassPathResource(privateKeyPath).getInputStream().readAllBytes());
                System.out.println("Using local private key for JWT signing");
            } else {
                // Production: Get from AWS Secrets Manager (parse JSON and extract private-key)
                keyContent = getAwsKeyMap().get("private-key");
                System.out.println("Using AWS Secrets Manager private key for JWT signing");
            }
            String privateKeyPEM = keyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error loading private key", e);
        }
    }

    /**
     * Returns the public key as a Java RSAPublicKey object for internal cryptographic operations (e.g., JWT verification).
     */
    private RSAPublicKey getPublicKey() {
        try {
            String keyContent;
            if (isLocalProfile()) {
                // Local development: Read from classpath
                keyContent = new String(new ClassPathResource(publicKeyPath).getInputStream().readAllBytes());
            } else {
                // Production: Get from AWS Secrets Manager (parse JSON and extract public-key)
                keyContent = getAwsKeyMap().get("public-key");
            }
            String publicKeyPEM = keyContent.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error loading public key", e);
        }
    }

    /**
     * Returns the public key as a PEM-formatted string for sharing with clients (e.g., via /auth/public-key endpoint).
     */
    public String getPublicKeyPEM() {
        try {
            if (isLocalProfile()) {
                return new String(new ClassPathResource(publicKeyPath).getInputStream().readAllBytes());
            } else {
                return getAwsKeyMap().get("public-key");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading public key", e);
        }
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(
            Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
        );
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}
