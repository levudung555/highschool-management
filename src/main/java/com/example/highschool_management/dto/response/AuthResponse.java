package com.example.highschool_management.dto.response;

import java.time.Instant;
import java.util.List;

public final class AuthResponse {
  private AuthResponse() {}

  public record NguoiDung(Long maTaiKhoan, String tenDangNhap, String hoTen, List<String> vaiTro) {}

  public record DangNhap(
      String accessToken, String tokenType, Instant expiresAt, NguoiDung nguoiDung) {}

  public record TuyChon(boolean local) {}
}
