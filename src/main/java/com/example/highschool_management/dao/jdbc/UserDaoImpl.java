package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.model.Role;
import com.example.highschool_management.model.User;
import com.example.highschool_management.utils.JdbcConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT t.MaTK, t.MaGV, t.TenDangNhap, t.MatKhauBam, t.HoTen, t.Email, t.TrangThai, t.TokenVersion,
                   v.MaVT, v.MaSoVaiTro, v.TenVaiTro
            FROM tai_khoan t
            LEFT JOIN tai_khoan_vai_tro tv ON t.MaTK = tv.MaTK
            LEFT JOIN vai_tro v ON tv.MaVT = v.MaVT
            WHERE t.TenDangNhap = ?
            """;
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                User user = null;
                Set<Role> roles = new HashSet<>();
                while (rs.next()) {
                    if (user == null) {
                        String trangThai = rs.getString("TrangThai");
                        user = User.builder()
                                .id(rs.getLong("MaTK"))
                                .maGV(rs.getObject("MaGV", Long.class))
                                .username(rs.getString("TenDangNhap"))
                                .password(rs.getString("MatKhauBam"))
                                .fullName(rs.getString("HoTen"))
                                .email(rs.getString("Email"))
                                .trangThai(trangThai)
                                .enabled("HOAT_DONG".equalsIgnoreCase(trangThai))
                                .tokenVersion(rs.getInt("TokenVersion"))
                                .roles(roles)
                                .build();
                    }
                    Long maVT = rs.getObject("MaVT", Long.class);
                    if (maVT != null) {
                        Role role = Role.builder()
                                .id(maVT)
                                .name(rs.getString("MaSoVaiTro"))
                                .description(rs.getString("TenVaiTro"))
                                .build();
                        roles.add(role);
                    }
                }
                return Optional.ofNullable(user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm tài khoản theo tên đăng nhập", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT EXISTS(SELECT 1 FROM tai_khoan WHERE TenDangNhap = ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại tên đăng nhập", e);
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT EXISTS(SELECT 1 FROM tai_khoan WHERE Email = ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại email", e);
        }
        return false;
    }

    @Override
    public User save(User user) {
        String insertUserSql = """
            INSERT INTO tai_khoan (MaGV, TenDangNhap, MatKhauBam, HoTen, Email, TrangThai, TokenVersion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        String insertRoleSql = "INSERT INTO tai_khoan_vai_tro (MaTK, MaVT) VALUES (?, ?)";

        try (Connection conn = JdbcConnect.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (user.getId() == null) {
                    try (PreparedStatement psUser = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                        if (user.getMaGV() != null) {
                            psUser.setLong(1, user.getMaGV());
                        } else {
                            psUser.setNull(1, Types.BIGINT);
                        }
                        psUser.setString(2, user.getUsername());
                        psUser.setString(3, user.getPassword());
                        psUser.setString(4, user.getFullName());
                        psUser.setString(5, user.getEmail());
                        psUser.setString(6, user.getTrangThai() != null ? user.getTrangThai() : (user.isEnabled() ? "HOAT_DONG" : "BI_KHOA"));
                        psUser.setInt(7, user.getTokenVersion() != null ? user.getTokenVersion() : 0);
                        psUser.executeUpdate();
                        try (ResultSet rs = psUser.getGeneratedKeys()) {
                            if (rs.next()) {
                                user.setId(rs.getLong(1));
                            }
                        }
                    }
                } else {
                    String updateUserSql = """
                        UPDATE tai_khoan
                        SET MaGV = ?, MatKhauBam = ?, HoTen = ?, Email = ?, TrangThai = ?, TokenVersion = ?
                        WHERE MaTK = ?
                        """;
                    try (PreparedStatement psUser = conn.prepareStatement(updateUserSql)) {
                        if (user.getMaGV() != null) {
                            psUser.setLong(1, user.getMaGV());
                        } else {
                            psUser.setNull(1, Types.BIGINT);
                        }
                        psUser.setString(2, user.getPassword());
                        psUser.setString(3, user.getFullName());
                        psUser.setString(4, user.getEmail());
                        psUser.setString(5, user.getTrangThai() != null ? user.getTrangThai() : (user.isEnabled() ? "HOAT_DONG" : "BI_KHOA"));
                        psUser.setInt(6, user.getTokenVersion() != null ? user.getTokenVersion() : 0);
                        psUser.setLong(7, user.getId());
                        psUser.executeUpdate();
                    }
                    try (PreparedStatement psDeleteRoles = conn.prepareStatement("DELETE FROM tai_khoan_vai_tro WHERE MaTK = ?")) {
                        psDeleteRoles.setLong(1, user.getId());
                        psDeleteRoles.executeUpdate();
                    }
                }

                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    try (PreparedStatement psRole = conn.prepareStatement(insertRoleSql)) {
                        for (Role role : user.getRoles()) {
                            if (role.getId() != null) {
                                psRole.setLong(1, user.getId());
                                psRole.setLong(2, role.getId());
                                psRole.addBatch();
                            }
                        }
                        psRole.executeBatch();
                    }
                }
                conn.commit();
                return user;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu tài khoản", e);
        }
    }
}
