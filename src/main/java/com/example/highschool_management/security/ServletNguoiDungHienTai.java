package com.example.highschool_management.security;

import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.exception.AccessException;
import com.example.highschool_management.model.User;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Lấy thông tin xác thực từ JWT đã được Spring Security xác minh. */
@Component
@RequiredArgsConstructor
public class ServletNguoiDungHienTai implements NguoiDungHienTai {

  private final UserDao userDao;

  @Override
  public long yeuCauVaiTro(String... roles) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || !(auth instanceof JwtAuthenticationToken jwt)) {
      throw new AccessException(401, "Cần đăng nhập");
    }
    boolean allowed =
        Arrays.stream(roles)
            .anyMatch(
                role ->
                    auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role)));
    if (!allowed) {
      throw new AccessException(403, "Không có quyền thực hiện thao tác");
    }
    String username = jwt.getToken().getSubject();
    return userDao.findByUsername(username).map(User::getId).orElse(1L);
  }
}
