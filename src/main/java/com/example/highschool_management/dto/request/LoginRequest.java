package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank(message = "Nhập tên đăng nhập") @Size(max = 100) String tenDangNhap,
    @NotBlank(message = "Nhập mật khẩu") @Size(max = 72) String matKhau) {}
