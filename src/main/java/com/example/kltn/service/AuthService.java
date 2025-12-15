package com.example.kltn.service;

import com.example.kltn.dto.request.*;
import com.example.kltn.dto.response.AuthResponse;

public interface AuthService {
    
    AuthResponse register(RegisterRequest request);
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse refreshToken(String refreshToken);
    
    void forgotPassword(String email);
    
    void resetPassword(ResetPasswordRequest request);
    
    void verifyEmail(String verificationCode);
    
    void logout(String token);
    
    void resendVerificationEmail(String email);
    
    void linkOAuth2Account(String provider, String accessToken, String jwtToken);
    
    void unlinkOAuth2Account(String provider, String jwtToken);
}