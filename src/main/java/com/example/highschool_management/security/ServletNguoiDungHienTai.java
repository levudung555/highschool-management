package com.example.highschool_management.security;

import com.example.highschool_management.exception.AccessException;
import java.util.Arrays;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Lấy MaTK từ JWT đã được Spring Security xác minh, áp dụng cả profile local. */
@Component
public class ServletNguoiDungHienTai implements NguoiDungHienTai {
  @Override
  public long yeuCauVaiTro(String... roles) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof JwtAuthenticationToken jwt) || !auth.isAuthenticated())
      throw new AccessException(401, "Cần đăng nhập");
    boolean allowed =
        Arrays.stream(roles)
            .anyMatch(
                role ->
                    auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role)));
    if (!allowed) throw new AccessException(403, "Không có quyền thực hiện thao tác");
    return Long.parseLong(jwt.getToken().getSubject());
  }
}
