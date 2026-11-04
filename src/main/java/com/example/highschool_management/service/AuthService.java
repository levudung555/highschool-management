package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.LoginRequest;
import com.example.highschool_management.dto.response.AuthResponse.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AuthService {
  DangNhap dangNhap(@Valid @NotNull LoginRequest request);

  NguoiDung hienTai();

  void dangXuat();
}
