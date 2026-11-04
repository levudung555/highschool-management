package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.LoginRequest;
import com.example.highschool_management.dto.response.AuthResponse.*;
import com.example.highschool_management.service.AuthService;
import jakarta.validation.Valid;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service;
  private final Environment env;

  public AuthController(AuthService service, Environment env) {
    this.service = service;
    this.env = env;
  }

  @PostMapping("/login")
  public ResponseEntity<DangNhap> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.dangNhap(request));
  }

  @GetMapping("/me")
  public ResponseEntity<NguoiDung> me() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.hienTai());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    service.dangXuat();
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/options")
  public TuyChon options() {
    return new TuyChon(Arrays.asList(env.getActiveProfiles()).contains("local"));
  }
}
