package com.example.demo.DTOs.ResponseDTO;

import lombok.*;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;       // "Bearer"
    private long   expiresIn;       // giây còn lại của access token
    private UserResponse user;
}