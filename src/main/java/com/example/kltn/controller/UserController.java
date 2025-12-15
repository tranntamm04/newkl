package com.example.kltn.controller;

import com.example.kltn.dto.request.ChangePasswordRequest;
import com.example.kltn.dto.request.UpdateProfileRequest;
import com.example.kltn.dto.response.ApiResponse;
import com.example.kltn.dto.response.UserResponse;
import com.example.kltn.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API quản lý người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader("Authorization") String token) {
        
        UserResponse userResponse = userService.getCurrentUser(token.substring(7));
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Lấy thông tin thành công"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String token) {
        
        UserResponse userResponse = userService.updateProfile(request, token.substring(7));
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Cập nhật thông tin thành công"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String token) {
        
        userService.changePassword(
            userService.getCurrentUser(token.substring(7)).getEmail(),
            request.getOldPassword(),
            request.getNewPassword()
        );
        
        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy danh sách tất cả người dùng (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công"));
    }

    @GetMapping("/by-role/{roleName}")
    @Operation(summary = "Lấy danh sách người dùng theo role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable String roleName) {
        
        List<UserResponse> users = userService.getUsersByRole("ROLE_" + roleName.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng theo role thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin người dùng theo ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Lấy thông tin người dùng thành công"));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Cập nhật roles cho người dùng (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long id,
            @RequestBody List<String> roles) {
        
        UserResponse userResponse = userService.updateUserRole(id, roles);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Cập nhật roles thành công"));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Vô hiệu hóa người dùng (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        UserResponse userResponse = userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Vô hiệu hóa người dùng thành công"));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Kích hoạt người dùng (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        UserResponse userResponse = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Kích hoạt người dùng thành công"));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm người dùng theo email (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@RequestParam String email) {
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Tìm kiếm người dùng thành công"));
    }
}