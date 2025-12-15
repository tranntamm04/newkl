package com.example.kltn.service;

public interface JwtService {
    
    String extractUsername(String token);
    
    String generateToken(String username);
    
    String generateRefreshToken(String username);
    
    boolean isTokenValid(String token, String username);
    
    void invalidateToken(String token);
    
    boolean isTokenExpired(String token);
    
    Long extractUserId(String token);
}