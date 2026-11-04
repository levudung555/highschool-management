package com.example.highschool_management.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
    @JsonAlias("tenDangNhap")
    @NotBlank(message = "Nhập tên đăng nhập") @Size(max = 100) String username,
    @JsonAlias("matKhau")
    @NotBlank(message = "Nhập mật khẩu") @Size(max = 72) String password
) {
  public String tenDangNhap() {
    return username;
  }

  public String matKhau() {
    return password;
  }
}
