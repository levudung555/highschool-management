package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.TeacherDao;
import com.example.highschool_management.model.Teacher;
import com.example.highschool_management.utils.JdbcConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherDaoImpl implements TeacherDao {

  @Override
  public Teacher insertTeacher(Teacher teacher) {
    String sql =
        """
        INSERT INTO GIAO_VIEN (MaSoGV, HoTen, NgaySinh, SoDienThoai, Email, ChuyenMon, TrangThai)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, teacher.getMaSoGV());
      ps.setString(2, teacher.getHoTen());
      ps.setDate(3, teacher.getNgaySinh());
      ps.setString(4, teacher.getSdt());
      ps.setString(5, teacher.getEmail());
      ps.setString(6, teacher.getChuyenMon());
      ps.setString(7, teacher.getTrangThai());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          teacher.setMaGV(rs.getLong(1));
        }
      }
      return teacher;
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi thêm giáo viên", e);
    }
  }

  @Override
  public boolean updateTeacher(Teacher teacher) {
    String sql =
        """
        UPDATE GIAO_VIEN
        SET HoTen = ?, NgaySinh = ?, SoDienThoai = ?, Email = ?, ChuyenMon = ?, TrangThai = ?
        WHERE MaGV = ?
        """;
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, teacher.getHoTen());
      ps.setDate(2, teacher.getNgaySinh());
      ps.setString(3, teacher.getSdt());
      ps.setString(4, teacher.getEmail());
      ps.setString(5, teacher.getChuyenMon());
      ps.setString(6, teacher.getTrangThai());
      ps.setLong(7, teacher.getMaGV());
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi cập nhật giáo viên", e);
    }
  }

  @Override
  public boolean existsSoDienThoai(String soDienThoai) {
    String sql = "SELECT EXISTS(SELECT 1 FROM GIAO_VIEN WHERE SoDienThoai = ?)";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, soDienThoai);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getBoolean(1);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi kiểm tra số điện thoại giáo viên", e);
    }
    return false;
  }

  @Override
  public boolean existsEmail(String email) {
    String sql = "SELECT EXISTS(SELECT 1 FROM GIAO_VIEN WHERE Email = ?)";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getBoolean(1);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi kiểm tra email giáo viên", e);
    }
    return false;
  }

  @Override
  public String getLastMaSoGV() {
    String sql =
        """
        SELECT MaSoGV
        FROM GIAO_VIEN
        WHERE REGEXP_LIKE(MaSoGV, '^GV[0-9]+$', 'c')
        ORDER BY CAST(SUBSTRING(MaSoGV, 3) AS DECIMAL(30, 0)) DESC
        LIMIT 1
        """;
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        return rs.getString("MaSoGV");
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi lấy mã giáo viên cuối cùng", e);
    }
    return null;
  }

  @Override
  public Optional<Teacher> findByMaGV(Long maGV) {
    String sql = "SELECT * FROM GIAO_VIEN WHERE MaGV = ?";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, maGV);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapResultSetToTeacher(rs));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi tìm giáo viên theo mã", e);
    }
    return Optional.empty();
  }

  @Override
  public boolean deleteTeachers(List<Long> maGV) {
    if (maGV == null || maGV.isEmpty()) {
      return false;
    }
    String sql =
        "DELETE FROM GIAO_VIEN WHERE MaGV IN ("
            + String.join(",", Collections.nCopies(maGV.size(), "?"))
            + ")";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 0; i < maGV.size(); i++) {
        ps.setLong(i + 1, maGV.get(i));
      }
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi xóa giáo viên", e);
    }
  }

  @Override
  public List<Long> findExistingIds(List<Long> maGV) {
    if (maGV == null || maGV.isEmpty()) {
      return Collections.emptyList();
    }
    String sql =
        "SELECT MaGV FROM GIAO_VIEN WHERE MaGV IN ("
            + String.join(",", Collections.nCopies(maGV.size(), "?"))
            + ")";
    List<Long> existingIds = new ArrayList<>();
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 0; i < maGV.size(); i++) {
        ps.setLong(i + 1, maGV.get(i));
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          existingIds.add(rs.getLong("MaGV"));
        }
      }
      return existingIds;
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi kiểm tra danh sách mã giáo viên", e);
    }
  }

  @Override
  public List<Teacher> findAll() {
    String sql = "SELECT * FROM GIAO_VIEN ORDER BY MaGV DESC";
    List<Teacher> list = new ArrayList<>();
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(mapResultSetToTeacher(rs));
      }
      return list;
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi lấy danh sách giáo viên", e);
    }
  }

  private Teacher mapResultSetToTeacher(ResultSet rs) throws Exception {
    Teacher teacher = new Teacher();
    teacher.setMaGV(rs.getLong("MaGV"));
    teacher.setMaSoGV(rs.getString("MaSoGV"));
    teacher.setHoTen(rs.getString("HoTen"));
    teacher.setNgaySinh(rs.getDate("NgaySinh"));
    teacher.setSdt(rs.getString("SoDienThoai"));
    teacher.setEmail(rs.getString("Email"));
    teacher.setChuyenMon(rs.getString("ChuyenMon"));
    teacher.setTrangThai(rs.getString("TrangThai"));
    return teacher;
  }
}
