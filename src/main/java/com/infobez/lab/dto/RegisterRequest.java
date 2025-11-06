package com.infobez.lab.dto;


import lombok.Data;

// DTO за регистрација
@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}

// DTO за најава
@Data
class LoginRequest {
    private String username;
    private String password;
}

// DTO за одговор
@Data
class AuthResponse {
    private String message;
    private boolean success;
    private String username;

    public AuthResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public AuthResponse(String message, boolean success, String username) {
        this.message = message;
        this.success = success;
        this.username = username;
    }
}