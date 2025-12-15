package com.example.kltn.controller;

import com.example.kltn.dto.response.ApiResponse;
import com.example.kltn.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final AuthService authService;

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOAuth2Providers() {
        Map<String, String> providers = Map.of(
            "google", "/oauth2/authorization/google",
            "facebook", "/oauth2/authorization/facebook"
        );
        return ResponseEntity.ok(ApiResponse.success(providers, "Danh sách OAuth2 providers"));
    }

    @PostMapping("/link-account")
    public ResponseEntity<ApiResponse<String>> linkOAuth2Account(
            @RequestParam String provider,
            @RequestParam String accessToken,
            @RequestHeader("Authorization") String jwtToken) {
        
        String token = jwtToken.substring(7);
        authService.linkOAuth2Account(provider, accessToken, token);
        return ResponseEntity.ok(ApiResponse.success(null, "Liên kết tài khoản OAuth2 thành công!"));
    }

    @PostMapping("/unlink-account")
    public ResponseEntity<ApiResponse<String>> unlinkOAuth2Account(
            @RequestParam String provider,
            @RequestHeader("Authorization") String jwtToken) {
        
        String token = jwtToken.substring(7);
        authService.unlinkOAuth2Account(provider, token);
        return ResponseEntity.ok(ApiResponse.success(null, "Hủy liên kết OAuth2 thành công!"));
    }
}