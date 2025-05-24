package com.gab.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Used for testing service layer, not for API. API uses Jackson to convert JSON to object.
public class LoginRequest {
    private String email;
    private String password;
}