package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.dto.request.LoginRequest;
import com.example.highschool_management.dto.response.AuthResponse.*;
import com.example.highschool_management.exception.*;
import com.example.highschool_management.model.UserAccount;
import com.example.highschool_management.security.*;
import com.example.highschool_management.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
  private final AuthDao dao;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final NguoiDungHienTai current;
  private final Clock clock;
  private final String dummy;

  public AuthServiceImpl(
      AuthDao dao,
      PasswordEncoder passwords,
      JwtEncoder encoder,
      NguoiDungHienTai current,
      Clock clock) {
    this.dao = dao;
    this.passwords = passwords;
    this.encoder = encoder;
    this.current = current;
    this.clock = clock;
    this.dummy = passwords.encode("not-a-real-user-password");
  }

  private NguoiDung response(UserAccount user) {
    return new NguoiDung(
        user.maTaiKhoan(),
        user.tenDangNhap(),
        user.hoTen(),
        dao.vaiTro(user.maTaiKhoan()).stream()
            .filter(SchoolSecurityConfig.ROLES::contains)
            .toList());
  }

  @Override
  public DangNhap dangNhap(LoginRequest request) {
    if (request.matKhau().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new InvalidRequestException("Mật khẩu tối đa 72 byte UTF-8");
    var user = dao.timTheoTen(request.tenDangNhap().trim()).orElse(null);
    String hash = user == null ? dummy : user.matKhauBam();
    if (hash.startsWith("{bcrypt}")) hash = hash.substring(8);
    boolean match = false;
    try {
      match = passwords.matches(request.matKhau(), hash);
    } catch (IllegalArgumentException ignored) {
    }
    if (!match || user == null || !"HOAT_DONG".equals(user.trangThai()))
      throw new AccessException(401, "Tên đăng nhập hoặc mật khẩu không đúng");
    var profile = response(user);
    if (profile.vaiTro().isEmpty())
      throw new AccessException(403, "Tài khoản chưa được phân quyền");
    Instant at = clock.instant(), end = at.plus(Duration.ofMinutes(30));
    var claims =
        JwtClaimsSet.builder()
            .issuer(SchoolSecurityConfig.ISSUER)
            .subject(user.maTaiKhoan().toString())
            .audience(List.of(SchoolSecurityConfig.AUDIENCE))
            .issuedAt(at)
            .expiresAt(end)
            .id(UUID.randomUUID().toString())
            .claim("ver", user.tokenVersion())
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new DangNhap(token, "Bearer", end, profile);
  }

  private long actor() {
    return current.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU", "GIAO_VIEN");
  }

  @Override
  public NguoiDung hienTai() {
    return response(
        dao.timTheoMa(actor())
            .orElseThrow(() -> new AccessException(401, "Tài khoản không còn tồn tại")));
  }

  @Override
  @Transactional
  public void dangXuat() {
    dao.thuHoiToken(actor());
  }
}
