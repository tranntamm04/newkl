package com.example.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", 
             message = "Số điện thoại không hợp lệ")
    private String phone;
    
    private String address;
    
    private String avatar;
}