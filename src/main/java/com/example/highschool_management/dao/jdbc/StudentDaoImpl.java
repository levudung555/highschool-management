package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.enums.SexEnum;
import com.example.highschool_management.model.Student;
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
public class StudentDaoImpl implements StudentDao {

  @Override
  public Student insertStudent(Student student) {
    String sql =
        "INSERT INTO hoc_sinh (MaSoHS, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, Email,"
            + " NgayTiepNhan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, student.getMaSoHS());
      ps.setString(2, student.getHoTen());
      ps.setDate(3, student.getNgaySinh());
      ps.setString(4, student.getGioiTinh().toString());
      ps.setString(5, student.getDiaChi());
      ps.setString(6, student.getSdt());
      ps.setString(7, student.getEmail());
      ps.setDate(8, student.getNgayTiepNhan());
      ps.executeUpdate();
      try (var rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          student.setMaHS(rs.getLong(1));
        }
      }
      return student;
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi thêm học sinh", e);
    }
  }

  @Override
  public boolean existsSoDienThoai(String soDienThoai) {
    String sql = "SELECT EXISTS(SELECT 1 FROM hoc_sinh WHERE SoDienThoai = ?)";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, soDienThoai);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getBoolean(1);
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi kiểm tra số điện thoại", e);
    }
    return false;
  }

  @Override
  public boolean existsEmail(String email) {
    String sql = "SELECT EXISTS(SELECT 1 FROM hoc_sinh WHERE Email = ?)";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getBoolean(1);
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi kiểm tra email", e);
    }
    return false;
  }

  @Override
  public String getLastMaSoHS() {
    String sql =
        """
        SELECT MaSoHS
        FROM hoc_sinh
        WHERE REGEXP_LIKE(MaSoHS, '^HS[0-9]+$', 'c')
        ORDER BY CAST(SUBSTRING(MaSoHS, 3) AS DECIMAL(30, 0)) DESC
        LIMIT 1
        """;

    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      if (rs.next()) {
        return rs.getString("MaSoHS");
      }

    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi lấy mã học sinh cuối cùng", e);
    }

    return null;
  }

  @Override
  public Optional<Student> findByMaHS(Long maHS) {
    String sql = "SELECT * FROM hoc_sinh WHERE MaHS = ?";
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, maHS);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Student student = new Student();

          student.setMaHS(rs.getLong("MaHS"));
          student.setMaSoHS(rs.getString("MaSoHS"));
          student.setHoTen(rs.getString("HoTen"));
          student.setNgaySinh(rs.getDate("NgaySinh"));
          student.setGioiTinh(SexEnum.valueOf(rs.getString("GioiTinh")));
          student.setDiaChi(rs.getString("DiaChi"));
          student.setSdt(rs.getString("SoDienThoai"));
          student.setEmail(rs.getString("Email"));
          student.setNgayTiepNhan(rs.getDate("NgayTiepNhan"));

          return Optional.of(student);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi lấy mã học sinh", e);
    }
    return Optional.empty();
  }

  @Override
  public List<Student> searchStudents(
      String maSoHS, String hoTen, Long maLop, Long maKhoi, Long maNamHoc) {
    StringBuilder sql = new StringBuilder("SELECT * FROM hoc_sinh WHERE 1=1");
    List<Object> params = new ArrayList<>();
    if (maSoHS != null && !maSoHS.trim().isEmpty()) {
      sql.append(" AND MaSoHS = ?");
      params.add(maSoHS.trim());
    }
    if (hoTen != null && !hoTen.trim().isEmpty()) {
      sql.append(" AND HoTen LIKE ?");
      params.add("%" + hoTen.trim() + "%");
    }
    if (maLop != null || maKhoi != null || maNamHoc != null) {
      sql.append(
          " AND EXISTS (SELECT 1 FROM xep_lop x JOIN lop_hoc l ON l.MaLop=x.MaLop WHERE"
              + " x.MaHS=hoc_sinh.MaHS AND x.TrangThaiDuyet='DA_DUYET' AND x.TuNgay<=CURRENT_DATE"
              + " AND (x.DenNgay IS NULL OR x.DenNgay>CURRENT_DATE)");
      if (maLop != null) {
        sql.append(" AND l.MaLop=?");
        params.add(maLop);
      }
      if (maKhoi != null) {
        sql.append(" AND l.MaKhoi=?");
        params.add(maKhoi);
      }
      if (maNamHoc != null) {
        sql.append(" AND l.MaNH=?");
        params.add(maNamHoc);
      }
      sql.append(")");
    }
    sql.append(" ORDER BY MaHS DESC");
    List<Student> result = new ArrayList<>();
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      for (int i = 0; i < params.size(); i++) {
        ps.setObject(i + 1, params.get(i));
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Student student = new Student();
          student.setMaHS(rs.getLong("MaHS"));
          student.setMaSoHS(rs.getString("MaSoHS"));
          student.setHoTen(rs.getString("HoTen"));
          student.setNgaySinh(rs.getDate("NgaySinh"));
          student.setGioiTinh(SexEnum.valueOf(rs.getString("GioiTinh")));
          student.setDiaChi(rs.getString("DiaChi"));
          student.setSdt(rs.getString("SoDienThoai"));
          student.setEmail(rs.getString("Email"));
          student.setNgayTiepNhan(rs.getDate("NgayTiepNhan"));
          result.add(student);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi tìm kiếm học sinh", e);
    }
    return result;
  }

  @Override
  public List<Student> findAll() {
    String sql = "SELECT * FROM hoc_sinh ORDER BY MaHS DESC";
    List<Student> result = new ArrayList<>();
    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        Student student = new Student();
        student.setMaHS(rs.getLong("MaHS"));
        student.setMaSoHS(rs.getString("MaSoHS"));
        student.setHoTen(rs.getString("HoTen"));
        student.setNgaySinh(rs.getDate("NgaySinh"));
        student.setGioiTinh(SexEnum.valueOf(rs.getString("GioiTinh")));
        student.setDiaChi(rs.getString("DiaChi"));
        student.setSdt(rs.getString("SoDienThoai"));
        student.setEmail(rs.getString("Email"));
        student.setNgayTiepNhan(rs.getDate("NgayTiepNhan"));
        result.add(student);
      }
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi lấy danh sách tất cả học sinh", e);
    }
    return result;
  }

  @Override
  public List<Long> findExistingIds(List<Long> maHS) {
    String sql =
        "SELECT MaHS FROM hoc_sinh WHERE MaHS IN ("
            + String.join(",", Collections.nCopies(maHS.size(), "?"))
            + ")";

    List<Long> existingIds = new ArrayList<>();

    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      for (int i = 0; i < maHS.size(); i++) {
        ps.setLong(i + 1, maHS.get(i));
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          existingIds.add(rs.getLong("MaHS"));
        }
      }

      return existingIds;

    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi kiểm tra danh sách học sinh", e);
    }
  }

  @Override
  public boolean deleteStudents(List<Long> maHS) {

    String sql =
        "DELETE FROM hoc_sinh WHERE MaHS IN ("
            + String.join(",", Collections.nCopies(maHS.size(), "?"))
            + ")";

    try (Connection conn = JdbcConnect.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      for (int i = 0; i < maHS.size(); i++) {
        ps.setLong(i + 1, maHS.get(i));
      }

      return ps.executeUpdate() > 0;

    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi xóa danh sách học sinh", e);
    }
  }

  @Override
  public void updateStudent(Student student) {
    String sql =
        "UPDATE hoc_sinh SET HoTen=?, NgaySinh=?, GioiTinh=?, DiaChi=?, SoDienThoai=?, Email=?"
            + " WHERE MaHS=?";
    try (Connection connection = JdbcConnect.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, student.getHoTen());
      statement.setDate(2, student.getNgaySinh());
      statement.setString(3, student.getGioiTinh().name());
      statement.setString(4, student.getDiaChi());
      statement.setString(5, student.getSdt());
      statement.setString(6, student.getEmail());
      statement.setLong(7, student.getMaHS());
      statement.executeUpdate();
    } catch (Exception exception) {
      throw new RuntimeException("Không thể cập nhật học sinh", exception);
    }
  }
}
