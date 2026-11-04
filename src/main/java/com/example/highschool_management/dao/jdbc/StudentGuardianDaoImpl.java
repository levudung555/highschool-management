package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.StudentGuardianDao;
import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.model.StudentGuardian;
import com.example.highschool_management.utils.JdbcConnect;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class StudentGuardianDaoImpl implements StudentGuardianDao {

    @Override
    public boolean link(StudentGuardian studentGuardian) {
        String sql = """
            INSERT INTO hs_nguoi_giam_ho (MaHS, MaNGH, MoiQuanHe, LienHeChinh)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE MoiQuanHe = VALUES(MoiQuanHe), LienHeChinh = VALUES(LienHeChinh)
            """;
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentGuardian.getMaHS());
            ps.setLong(2, studentGuardian.getMaNGH());
            ps.setString(3, studentGuardian.getMoiQuanHe());
            ps.setInt(4, Boolean.TRUE.equals(studentGuardian.getLienHeChinh()) ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi liên kết học sinh và người giám hộ", e);
        }
    }

    @Override
    public boolean unlink(Long maHS, Long maNGH) {
        String sql = "DELETE FROM hs_nguoi_giam_ho WHERE MaHS = ? AND MaNGH = ?";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maHS);
            ps.setLong(2, maNGH);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi hủy liên kết người giám hộ và học sinh", e);
        }
    }

    @Override
    public boolean existsByMaHSAndMaNGH(Long maHS, Long maNGH) {
        String sql = "SELECT EXISTS(SELECT 1 FROM hs_nguoi_giam_ho WHERE MaHS = ? AND MaNGH = ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maHS);
            ps.setLong(2, maNGH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra liên kết người giám hộ", e);
        }
        return false;
    }

    @Override
    public List<GuardianResponse> findGuardiansByMaHS(Long maHS) {
        String sql = """
            SELECT g.MaNGH, g.HoTen, g.SoDienThoai, g.Email, g.DiaChi,
                   sg.MoiQuanHe, sg.LienHeChinh
            FROM nguoi_giam_ho g
            INNER JOIN hs_nguoi_giam_ho sg ON g.MaNGH = sg.MaNGH
            WHERE sg.MaHS = ?
            ORDER BY sg.LienHeChinh DESC, g.MaNGH ASC
            """;
        List<GuardianResponse> list = new ArrayList<>();
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maHS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GuardianResponse res = new GuardianResponse();
                    res.setMaNGH(rs.getLong("MaNGH"));
                    res.setHoTen(rs.getString("HoTen"));
                    res.setDiaChi(rs.getString("DiaChi"));
                    res.setSdt(rs.getString("SoDienThoai"));
                    res.setEmail(rs.getString("Email"));
                    res.setMoiQuanHe(rs.getString("MoiQuanHe"));
                    res.setLienHeChinh(rs.getInt("LienHeChinh") == 1);
                    list.add(res);
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm danh sách người giám hộ của học sinh", e);
        }
    }

    @Override
    public boolean deleteByMaNGHs(List<Long> maNGHs) {
        if (maNGHs == null || maNGHs.isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM HS_NGUOI_GIAM_HO WHERE MaNGH IN ("
                + String.join(",", Collections.nCopies(maNGHs.size(), "?"))
                + ")";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < maNGHs.size(); i++) {
                ps.setLong(i + 1, maNGHs.get(i));
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa liên kết theo mã người giám hộ", e);
        }
    }

    @Override
    public boolean deleteByMaHSs(List<Long> maHSs) {
        if (maHSs == null || maHSs.isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM HS_NGUOI_GIAM_HO WHERE MaHS IN ("
                + String.join(",", Collections.nCopies(maHSs.size(), "?"))
                + ")";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < maHSs.size(); i++) {
                ps.setLong(i + 1, maHSs.get(i));
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa liên kết theo mã học sinh", e);
        }
    }
}
