package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.GuardianDao;
import com.example.highschool_management.model.Guardian;
import com.example.highschool_management.utils.JdbcConnect;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class GuardianDaoImpl implements GuardianDao {

    @Override
    public Guardian insertGuardian(Guardian guardian) {
        String sql = "INSERT INTO nguoi_giam_ho (HoTen, SoDienThoai, Email, DiaChi) VALUES (?, ?, ?, ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guardian.getHoTen());
            ps.setString(2, guardian.getSdt());
            ps.setString(3, guardian.getEmail());
            ps.setString(4, guardian.getDiaChi());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    guardian.setMaNGH(rs.getLong(1));
                }
            }
            return guardian;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi thêm người giám hộ", e);
        }
    }

    @Override
    public boolean updateGuardian(Guardian guardian) {
        String sql = "UPDATE nguoi_giam_ho SET HoTen = ?, SoDienThoai = ?, Email = ?, DiaChi = ? WHERE MaNGH = ?";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guardian.getHoTen());
            ps.setString(2, guardian.getSdt());
            ps.setString(3, guardian.getEmail());
            ps.setString(4, guardian.getDiaChi());
            ps.setLong(5, guardian.getMaNGH());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật thông tin người giám hộ", e);
        }
    }

    @Override
    public boolean existsSoDienThoai(String soDienThoai) {
        String sql = "SELECT EXISTS(SELECT 1 FROM nguoi_giam_ho WHERE SoDienThoai = ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra số điện thoại người giám hộ", e);
        }
        return false;
    }

    @Override
    public boolean existsEmail(String email) {
        String sql = "SELECT EXISTS(SELECT 1 FROM nguoi_giam_ho WHERE Email = ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra email người giám hộ", e);
        }
        return false;
    }

    @Override
    public Optional<Guardian> findByMaNGH(Long maNGH) {
        String sql = "SELECT * FROM nguoi_giam_ho WHERE MaNGH = ?";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maNGH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm người giám hộ theo mã", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteGuardians(List<Long> maNGH) {
        if (maNGH == null || maNGH.isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM NGUOI_GIAM_HO WHERE MaNGH IN ("
                + String.join(",", Collections.nCopies(maNGH.size(), "?"))
                + ")";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < maNGH.size(); i++) {
                ps.setLong(i + 1, maNGH.get(i));
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa người giám hộ", e);
        }
    }

    @Override
    public List<Long> findExistingIds(List<Long> maNGH) {
        if (maNGH == null || maNGH.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT MaNGH FROM NGUOI_GIAM_HO WHERE MaNGH IN ("
                + String.join(",", Collections.nCopies(maNGH.size(), "?"))
                + ")";
        List<Long> existingIds = new ArrayList<>();
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < maNGH.size(); i++) {
                ps.setLong(i + 1, maNGH.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingIds.add(rs.getLong("MaNGH"));
                }
            }
            return existingIds;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra danh sách mã người giám hộ", e);
        }
    }

    @Override
    public List<Guardian> findAll() {
        String sql = "SELECT * FROM nguoi_giam_ho ORDER BY MaNGH DESC";
        List<Guardian> list = new ArrayList<>();
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGuardian(rs));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách người giám hộ", e);
        }
    }

    private Guardian mapResultSetToGuardian(ResultSet rs) throws Exception {
        Guardian guardian = new Guardian();
        guardian.setMaNGH(rs.getLong("MaNGH"));
        guardian.setHoTen(rs.getString("HoTen"));
        guardian.setDiaChi(rs.getString("DiaChi"));
        guardian.setSdt(rs.getString("SoDienThoai"));
        guardian.setEmail(rs.getString("Email"));
        return guardian;
    }
}
