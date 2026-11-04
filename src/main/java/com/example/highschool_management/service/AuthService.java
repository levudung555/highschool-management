package com.example.highschool_management.service;

import com.example.highschool_management.dto.auth.request.AuthenticationRequest;
import com.example.highschool_management.dto.auth.response.AuthenticationResponse;
import com.example.highschool_management.dto.request.IntrospectRequest;
import com.example.highschool_management.dto.request.LogoutRequest;
import com.example.highschool_management.dto.request.RefreshRequest;
import com.example.highschool_management.dto.response.AuthResponse.*;
import com.example.highschool_management.dto.response.IntrospectResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AuthService {
  AuthenticationResponse authenticate(@Valid @NotNull AuthenticationRequest request);

  DangNhap dangNhap(@Valid @NotNull AuthenticationRequest request);

  IntrospectResponse introspect(IntrospectRequest request);

  void dangXuat(LogoutRequest request);

  void dangXuat();

  DangNhap refreshToken(RefreshRequest request);

  NguoiDung hienTai();
}
