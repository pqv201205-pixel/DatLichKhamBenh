package com.example.demo.DTOs.ResponseDTO;

import com.example.demo.Entities.User;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Integer       userId;
    private String        username;
    private String        email;
    private String        phoneNumber;
    private String        role;
    private Boolean       isActive;
    private LocalDateTime createdAt;

    // Không bao giờ trả passwordHash ra ngoài
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}