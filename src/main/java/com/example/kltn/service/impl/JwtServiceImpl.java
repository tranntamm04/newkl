package com.example.kltn.service.impl;

import com.example.kltn.entity.User;
import com.example.kltn.repository.UserRepository;
import com.example.kltn.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final UserRepository userRepository;
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;
    
    private Key key;
    private final Map<String, String> tokenBlacklist = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
    
    @Override
    public String generateToken(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
        
        return buildToken(claims, username, jwtExpiration);
    }
    
    @Override
    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, refreshExpiration);
    }
    
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    @Override
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        
        // Kiểm tra token có trong blacklist không
        if (tokenBlacklist.containsKey(token)) {
            log.warn("Token is blacklisted: {}", token);
            return false;
        }
        
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    @Override
    public void invalidateToken(String token) {
        if (token != null && !token.isEmpty()) {
            tokenBlacklist.put(token, "invalidated");
            log.info("Token invalidated: {}", token);
        }
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid token format: {}", e.getMessage());
            throw new RuntimeException("Token không hợp lệ");
        } catch (SecurityException e) {
            log.error("Invalid token signature: {}", e.getMessage());
            throw new RuntimeException("Chữ ký token không hợp lệ");
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            throw new RuntimeException("Lỗi xác thực token");
        }
    }
}