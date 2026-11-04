package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.AuthDao;
import com.example.highschool_management.model.UserAccount;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

@Repository
public class AuthDaoImpl implements AuthDao {
  private final JdbcTemplate jdbc;

  public AuthDaoImpl(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final String SELECT =
      "SELECT MaTK,TenDangNhap,MatKhauBam,HoTen,TrangThai,TokenVersion FROM tai_khoan";
  private static final RowMapper<UserAccount> MAPPER =
      (r, n) ->
          new UserAccount(
              r.getLong(1),
              r.getString(2),
              r.getString(3),
              r.getString(4),
              r.getString(5),
              r.getLong(6));

  @Override
  public Optional<UserAccount> timTheoTen(String username) {
    return jdbc.query(SELECT + " WHERE TenDangNhap=?", MAPPER, username).stream().findFirst();
  }

  @Override
  public Optional<UserAccount> timTheoMa(long id) {
    return jdbc.query(SELECT + " WHERE MaTK=?", MAPPER, id).stream().findFirst();
  }

  @Override
  public List<String> vaiTro(long id) {
    return jdbc.query(
        """
        SELECT v.MaSoVaiTro FROM tai_khoan_vai_tro t JOIN vai_tro v ON v.MaVT=t.MaVT WHERE t.MaTK=? ORDER BY v.MaSoVaiTro
        """,
        (r, n) -> r.getString(1),
        id);
  }

  @Override
  public void thuHoiToken(long id) {
    jdbc.update("UPDATE tai_khoan SET TokenVersion=TokenVersion+1 WHERE MaTK=?", id);
  }
}
