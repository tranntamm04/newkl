package com.example.kltn.controller;

import com.example.kltn.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public APIs", description = "API công khai không cần xác thực")
public class PublicController {

    @GetMapping("/health")
    @Operation(summary = "Kiểm tra tình trạng hệ thống")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> healthStatus = Map.of(
            "status", "UP",
            "service", "Fashion Store API",
            "version", "1.0.0",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(ApiResponse.success(healthStatus, "Hệ thống đang hoạt động bình thường"));
    }

    @GetMapping("/info")
    @Operation(summary = "Thông tin hệ thống")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSystemInfo() {
        Map<String, String> systemInfo = Map.of(
            "name", "Fashion Store",
            "description", "Hệ thống bán quần áo trực tuyến",
            "version", "1.0.0",
            "author", "Fashion Store Team",
            "contact", "support@fashionstore.com",
            "website", "https://fashionstore.com"
        );
        
        return ResponseEntity.ok(ApiResponse.success(systemInfo, "Thông tin hệ thống"));
    }
}