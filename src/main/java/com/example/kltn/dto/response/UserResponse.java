package com.example.kltn.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatar;
    private String provider;
    private Boolean emailVerified;
    private Boolean isActive;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}