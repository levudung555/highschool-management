package com.example.highschool_management.controller;

import com.example.highschool_management.dto.ApiResponse;
import com.example.highschool_management.dto.auth.request.AuthenticationRequest;
import com.example.highschool_management.dto.auth.response.AuthenticationResponse;
import com.example.highschool_management.dto.request.IntrospectRequest;
import com.example.highschool_management.dto.request.LogoutRequest;
import com.example.highschool_management.dto.request.RefreshRequest;
import com.example.highschool_management.dto.response.AuthResponse.*;
import com.example.highschool_management.dto.response.IntrospectResponse;
import com.example.highschool_management.service.AuthService;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationController {
  AuthService authService;
  Environment env;

  @PostMapping({"/auth/token", "/api/v1/auth/token"})
  public ApiResponse<AuthenticationResponse> token(
      @Valid @RequestBody AuthenticationRequest authenticationRequest) {
    return ApiResponse.<AuthenticationResponse>builder()
        .result(authService.authenticate(authenticationRequest))
        .build();
  }

  @PostMapping("/api/v1/auth/login")
  public ResponseEntity<DangNhap> login(@Valid @RequestBody AuthenticationRequest request) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(authService.dangNhap(request));
  }

  @PostMapping("/auth/introspect")
  public ApiResponse<IntrospectResponse> introspect(
      @RequestBody IntrospectRequest introspectRequest) {
    return ApiResponse.<IntrospectResponse>builder()
        .result(authService.introspect(introspectRequest))
        .build();
  }

  @PostMapping("/api/v1/auth/introspect")
  public ResponseEntity<IntrospectResponse> introspectV1(
      @RequestBody IntrospectRequest introspectRequest) {
    return ResponseEntity.ok(authService.introspect(introspectRequest));
  }

  @PostMapping("/auth/logout")
  public ApiResponse<Void> logout(@RequestBody(required = false) LogoutRequest logoutRequest) {
    authService.dangXuat(logoutRequest);
    return ApiResponse.<Void>builder().build();
  }

  @PostMapping("/api/v1/auth/logout")
  public ResponseEntity<Void> logoutV1(@RequestBody(required = false) LogoutRequest logoutRequest) {
    authService.dangXuat(logoutRequest);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/auth/refresh")
  public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
    var refreshed = authService.refreshToken(request);
    return ApiResponse.<AuthenticationResponse>builder()
        .result(
            AuthenticationResponse.builder()
                .token(refreshed.accessToken())
                .authenticated(true)
                .build())
        .build();
  }

  @PostMapping("/api/v1/auth/refresh")
  public ResponseEntity<DangNhap> refreshV1(@RequestBody RefreshRequest request) {
    return ResponseEntity.ok(authService.refreshToken(request));
  }

  @GetMapping({"/api/v1/auth/me", "/auth/me"})
  public ResponseEntity<NguoiDung> me() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(authService.hienTai());
  }

  @GetMapping({"/api/v1/auth/options", "/auth/options"})
  public TuyChon options() {
    return new TuyChon(Arrays.asList(env.getActiveProfiles()).contains("local"));
  }
}
