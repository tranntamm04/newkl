package com.example.kltn.controller;

import com.example.kltn.dto.response.ApiResponse;
import com.example.kltn.dto.response.UserResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.kltn.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "API dành cho quản trị viên")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/dashboard")
    @Operation(summary = "Thống kê dashboard admin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        // Thông tin thống kê
        Map<String, Object> dashboardData = Map.of(
            "totalUsers", 150,
            "newUsersToday", 5,
            "activeUsers", 145,
            "totalRevenue", 50000000,
            "todayRevenue", 1500000,
            "pendingOrders", 12,
            "completedOrders", 98,
            "lowStockProducts", 8
        );
        
        return ResponseEntity.ok(ApiResponse.success(dashboardData, "Lấy dashboard thành công"));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin admin hiện tại")
    public ResponseEntity<ApiResponse<UserResponse>> getAdminProfile(
            @RequestHeader("Authorization") String token) {
        
        UserResponse userResponse = userService.getCurrentUser(token.substring(7));
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Lấy thông tin admin thành công"));
    }
}