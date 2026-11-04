package com.example.highschool_management.security;

import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.exception.AccessException;
import com.example.highschool_management.model.UserAccount;
import java.util.Arrays;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Lấy MaTK từ JWT đã được Spring Security xác minh, áp dụng cả profile local. */
@Component
public class ServletNguoiDungHienTai implements NguoiDungHienTai {
  private final AuthDao dao;

  public ServletNguoiDungHienTai(AuthDao dao) {
    this.dao = dao;
  }

  @Override
  public long yeuCauVaiTro(String... roles) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof JwtAuthenticationToken jwt) || !auth.isAuthenticated()) {
      throw new AccessException(401, "Cần đăng nhập");
    }
    String subject = jwt.getToken().getSubject();
    long actorId;
    try {
      actorId = Long.parseLong(subject);
    } catch (NumberFormatException e) {
      actorId = dao.timTheoTen(subject).map(UserAccount::maTaiKhoan).orElse(0L);
    }
    var currentRoles = dao.vaiTro(actorId);
    boolean allowed = Arrays.stream(roles).anyMatch(currentRoles::contains);
    if (!allowed && currentRoles.isEmpty() && actorId == 0L) {
      allowed =
          Arrays.stream(roles)
              .anyMatch(
                  role ->
                      auth.getAuthorities().stream()
                          .anyMatch(a -> a.getAuthority().equals("ROLE_" + role)));
    }
    if (!allowed) throw new AccessException(403, "Không có quyền thực hiện thao tác");
    return actorId;
  }
}
