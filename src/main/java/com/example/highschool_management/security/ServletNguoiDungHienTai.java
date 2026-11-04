package com.example.highschool_management.security;

import com.example.highschool_management.exception.AccessException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServletNguoiDungHienTai implements NguoiDungHienTai {
  private final HttpServletRequest request;
  private final JdbcTemplate jdbc;

  public ServletNguoiDungHienTai(HttpServletRequest request, JdbcTemplate jdbc) {
    this.request = request;
    this.jdbc = jdbc;
  }

  @Override
  public long yeuCauVaiTro(String... vaiTro) {
    var principal = request.getUserPrincipal();
    if (principal == null) throw new AccessException(401, "Cần đăng nhập");
    // Integration contract: Principal.name is the local TenDangNhap, not a JWT sub.
    var ids =
        jdbc.query(
            "SELECT MaTK FROM tai_khoan WHERE TenDangNhap = ? AND TrangThai = 'HOAT_DONG'",
            (rs, row) -> rs.getLong(1),
            principal.getName());
    if (ids.isEmpty())
      throw new AccessException(403, "Tài khoản không hoạt động hoặc chưa được liên kết");
    long id = ids.getFirst();
    var roles =
        jdbc.query(
            """
            SELECT v.MaSoVaiTro FROM tai_khoan_vai_tro t
            JOIN vai_tro v ON v.MaVT = t.MaVT WHERE t.MaTK = ?
            """,
            (rs, row) -> rs.getString(1),
            id);
    if (Arrays.stream(vaiTro).noneMatch(roles::contains))
      throw new AccessException(403, "Không có quyền thực hiện thao tác");
    return id;
  }
}
