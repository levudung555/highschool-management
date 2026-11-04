package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.dto.auth.request.AuthenticationRequest;
import com.example.highschool_management.dto.auth.response.AuthenticationResponse;
import com.example.highschool_management.dto.request.IntrospectRequest;
import com.example.highschool_management.dto.request.LogoutRequest;
import com.example.highschool_management.dto.request.RefreshRequest;
import com.example.highschool_management.dto.response.AuthResponse.*;
import com.example.highschool_management.dto.response.IntrospectResponse;
import com.example.highschool_management.entity.Role;
import com.example.highschool_management.entity.User;
import com.example.highschool_management.exception.AccessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.model.UserAccount;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.security.SchoolSecurityConfig;
import com.example.highschool_management.service.AuthService;
import com.example.highschool_management.service.JwtTokenService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

  @Value("${jwt.valid-duration:3600}")
  private Long validDuration;

  @Value("${jwt.refreshable-duration:7200}")
  private Long refreshableDuration;

  private final AuthDao dao;
  private final PasswordEncoder passwords;
  private final JwtTokenService jwtTokenService;
  private final NguoiDungHienTai current;
  private final String dummy;

  public AuthServiceImpl(
      AuthDao dao,
      PasswordEncoder passwords,
      JwtTokenService jwtTokenService,
      NguoiDungHienTai current) {
    this.dao = dao;
    this.passwords = passwords;
    this.jwtTokenService = jwtTokenService;
    this.current = current;
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
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    if (request == null || request.username() == null || request.password() == null) {
      throw new AccessException(401, "Tên đăng nhập hoặc mật khẩu không đúng");
    }
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new InvalidRequestException("Mật khẩu tối đa 72 byte UTF-8");
    }
    var user = dao.timTheoTen(request.username().trim()).orElse(null);
    String hash = user == null ? dummy : user.matKhauBam();
    if (hash != null && hash.startsWith("{bcrypt}")) {
      hash = hash.substring(8);
    }
    boolean match = false;
    try {
      match = passwords.matches(request.password(), hash);
    } catch (IllegalArgumentException ignored) {
    }
    if (!match || user == null || !"HOAT_DONG".equals(user.trangThai())) {
      throw new AccessException(401, "Tên đăng nhập hoặc mật khẩu không đúng");
    }
    var roles = dao.vaiTro(user.maTaiKhoan()).stream()
        .filter(SchoolSecurityConfig.ROLES::contains)
        .toList();
    if (roles.isEmpty()) {
      throw new AccessException(403, "Tài khoản chưa được phân quyền");
    }
    User authUser =
        User.builder()
            .username(user.tenDangNhap())
            .roles(roles.stream().map(r -> Role.builder().name(r).build()).collect(Collectors.toSet()))
            .build();
    String token = jwtTokenService.generateAuthToken(authUser, validDuration);
    return AuthenticationResponse.builder().token(token).authenticated(true).build();
  }

  @Override
  public DangNhap dangNhap(AuthenticationRequest request) {
    var authResponse = authenticate(request);
    var user = dao.timTheoTen(request.username().trim()).orElseThrow();
    var profile = response(user);
    Instant expiresAt = Instant.now().plusSeconds(validDuration);
    return new DangNhap(authResponse.getToken(), "Bearer", expiresAt, profile);
  }

  @Override
  public IntrospectResponse introspect(IntrospectRequest request) {
    var token = request != null ? request.getToken() : null;
    boolean isValid = true;
    try {
      if (token == null || token.isBlank()) {
        isValid = false;
      } else {
        jwtTokenService.verifierToken(token, false);
      }
    } catch (Exception e) {
      isValid = false;
    }
    return IntrospectResponse.builder().valid(isValid).build();
  }

  @Override
  @Transactional
  public void dangXuat(LogoutRequest request) {
    try {
      if (request != null && request.getToken() != null && !request.getToken().isBlank()) {
        jwtTokenService.revokeToken(request.getToken());
      }
    } catch (Exception e) {
      log.warn("Token revocation failed: {}", e.getMessage());
    }

    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwt) {
      try {
        jwtTokenService.revokeToken(jwt.getToken().getTokenValue());
      } catch (Exception e) {
        log.warn("Current token revocation failed: {}", e.getMessage());
      }
    }
  }

  @Override
  @Transactional
  public void dangXuat() {
    dangXuat(null);
  }

  @Override
  public DangNhap refreshToken(RefreshRequest request) {
    try {
      var signedToken = jwtTokenService.verifierToken(request.getToken(), true);
      jwtTokenService.revokeToken(request.getToken());
      String subject = signedToken.getJWTClaimsSet().getSubject();
      UserAccount user = null;
      try {
        long userId = Long.parseLong(subject);
        user = dao.timTheoMa(userId).orElse(null);
      } catch (NumberFormatException ignored) {
      }
      if (user == null) {
        user = dao.timTheoTen(subject).orElse(null);
      }
      if (user == null) {
        throw new AccessException(401, "Tài khoản không tồn tại");
      }
      if (!"HOAT_DONG".equals(user.trangThai())) {
        throw new AccessException(401, "Tài khoản đã bị khóa");
      }
      var roles = dao.vaiTro(user.maTaiKhoan()).stream()
          .filter(SchoolSecurityConfig.ROLES::contains)
          .toList();
      User authUser =
          User.builder()
              .username(user.tenDangNhap())
              .roles(roles.stream().map(r -> Role.builder().name(r).build()).collect(Collectors.toSet()))
              .build();
      var token = jwtTokenService.generateAuthToken(authUser, refreshableDuration);
      Instant expiresAt = Instant.now().plusSeconds(refreshableDuration);
      return new DangNhap(token, "Bearer", expiresAt, response(user));
    } catch (AccessException e) {
      throw e;
    } catch (Exception e) {
      log.error("Refresh token error", e);
      throw new AccessException(401, "Token làm mới không hợp lệ");
    }
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
}
