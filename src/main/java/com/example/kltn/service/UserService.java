package com.example.kltn.service;

import com.example.kltn.dto.request.UpdateProfileRequest;
import com.example.kltn.dto.response.UserResponse;
import com.example.kltn.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    
    UserResponse getCurrentUser(String token);
    
    UserResponse updateProfile(UpdateProfileRequest request, String token);
    
    UserResponse getUserById(Long id);
    
    UserResponse getUserByEmail(String email);
    
    List<UserResponse> getAllUsers();
    
    List<UserResponse> getUsersByRole(String roleName);
    
    UserResponse updateUserRole(Long userId, List<String> roles);
    
    UserResponse deactivateUser(Long userId);
    
    UserResponse activateUser(Long userId);
    
    void changePassword(String email, String oldPassword, String newPassword);
    
    User getCurrentUserEntity(String token);
    
    void processOAuthPostLogin(String email, String name, String picture);
}